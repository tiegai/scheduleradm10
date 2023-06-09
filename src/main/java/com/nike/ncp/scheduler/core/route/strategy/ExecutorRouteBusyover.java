package com.nike.ncp.scheduler.core.route.strategy;

import com.nike.ncp.scheduler.common.biz.model.IdleBeatParam;
import com.nike.ncp.scheduler.core.route.ExecutorRouter;
import com.nike.ncp.scheduler.core.scheduler.XxlJobScheduler;
import com.nike.ncp.scheduler.core.util.I18nUtil;
import com.nike.ncp.scheduler.common.biz.ExecutorBiz;
import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.biz.model.TriggerParam;

import java.util.List;


public class ExecutorRouteBusyover extends ExecutorRouter {

    @Override
    @SuppressWarnings("all")
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        StringBuffer idleBeatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ReturnT<String> idleBeatResult = null;
            try {
                ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(address);
                idleBeatResult = executorBiz.idleBeat(new IdleBeatParam(triggerParam.getJobId()));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                idleBeatResult = new ReturnT<String>(ReturnT.FAIL_CODE, "" + e);
            }
            idleBeatResultSB.append((idleBeatResultSB.length() > 0) ? "<br><br>" : "")
                    .append(I18nUtil.getString("jobconf_idleBeat") + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(idleBeatResult.getCode())
                    .append("<br>msg：").append(idleBeatResult.getMsg());

            // beat success
            if (idleBeatResult.getCode() == ReturnT.SUCCESS_CODE) {
                idleBeatResult.setMsg(idleBeatResultSB.toString());
                idleBeatResult.setContent(address);
                return idleBeatResult;
            }
        }

        return new ReturnT<String>(ReturnT.FAIL_CODE, idleBeatResultSB.toString());
    }

}
