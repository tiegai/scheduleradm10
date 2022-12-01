package com.nike.springboottemplate.core.alarm;

import com.nike.springboottemplate.core.model.XxlJobInfo;
import com.nike.springboottemplate.core.model.XxlJobLog;


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
