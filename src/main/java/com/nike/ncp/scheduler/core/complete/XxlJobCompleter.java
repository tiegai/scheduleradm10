package com.nike.ncp.scheduler.core.complete;

import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.context.XxlJobContext;
import com.nike.ncp.scheduler.core.conf.XxlJobAdminConfig;
import com.nike.ncp.scheduler.core.model.XxlJobInfo;
import com.nike.ncp.scheduler.core.model.XxlJobLog;
import com.nike.ncp.scheduler.core.thread.JobTriggerPoolHelper;
import com.nike.ncp.scheduler.core.trigger.TriggerTypeEnum;
import com.nike.ncp.scheduler.core.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public final class XxlJobCompleter {
    private static final Logger LOGGER = LoggerFactory.getLogger(XxlJobCompleter.class);

    private XxlJobCompleter() {

    }
    private static final int HANDLE_MSG_LENGTH = 15000;

    /**
     * common fresh handle entrance (limit only once)
     *
     * @param xxlJobLog
     * @return
     */
    public static int updateHandleInfoAndFinish(XxlJobLog xxlJobLog) {

        // finish
        finishJob(xxlJobLog);

        // text最大64kb 避免长度过长
        if (xxlJobLog.getHandleMsg().length() > HANDLE_MSG_LENGTH) {
            xxlJobLog.setHandleMsg(xxlJobLog.getHandleMsg().substring(0, 15000));
        }

        // fresh handle
        return XxlJobAdminConfig.getAdminConfig().getXxlJobLogDao().updateHandleInfo(xxlJobLog);
    }


    /**
     * do somethind to finish job
     */
    @SuppressWarnings("all")
    private static void finishJob(XxlJobLog xxlJobLog) {

        // 1、handle success, to trigger child job
        String triggerChildMsg = null;
        if (XxlJobContext.HANDLE_CODE_SUCCESS == xxlJobLog.getHandleCode()) {
            XxlJobInfo xxlJobInfo = XxlJobAdminConfig.getAdminConfig().getXxlJobInfoDao().loadById(xxlJobLog.getJobId());
            if (xxlJobInfo != null && xxlJobInfo.getChildJobId() != null && xxlJobInfo.getChildJobId().trim().length() > 0) {
                triggerChildMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_child_run") + "<<<<<<<<<<< </span><br>";

                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (childJobIds[i] != null && childJobIds[i].trim().length() > 0 && isNumeric(childJobIds[i])) ? Integer.parseInt(childJobIds[i]) : -1;
                    if (childJobId > 0) {

                        JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null, null);
                        ReturnT<String> triggerChildResult = ReturnT.SUCCESS;

                        // add msg
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"), (i + 1), childJobIds.length, childJobIds[i], (triggerChildResult.getCode() == ReturnT.SUCCESS_CODE ? I18nUtil.getString("system_success") : I18nUtil.getString("system_fail")), triggerChildResult.getMsg());
                    } else {
                        triggerChildMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"), (i + 1), childJobIds.length, childJobIds[i]);
                    }
                }

            }
        }

        if (triggerChildMsg != null) {
            xxlJobLog.setHandleMsg(xxlJobLog.getHandleMsg() + triggerChildMsg);
        }

        // 2、fix_delay trigger next
        // on the way

    }

    private static boolean isNumeric(String str) {
        try {
            //int result = Integer.valueOf(str);
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
