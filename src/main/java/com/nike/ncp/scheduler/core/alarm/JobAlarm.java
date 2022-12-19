package com.nike.ncp.scheduler.core.alarm;

import com.nike.ncp.scheduler.core.model.XxlJobInfo;
import com.nike.ncp.scheduler.core.model.XxlJobLog;


public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}
