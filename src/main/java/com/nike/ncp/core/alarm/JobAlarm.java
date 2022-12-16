package com.nike.ncp.core.alarm;

import com.nike.ncp.core.model.XxlJobInfo;
import com.nike.ncp.core.model.XxlJobLog;


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
