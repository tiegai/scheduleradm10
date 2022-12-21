package com.nike.ncp.scheduler.service;

import com.nike.ncp.scheduler.core.model.JourneyInfo;
import com.nike.ncp.scheduler.core.model.JourneyLogRes;
import com.nike.ncp.scheduler.core.model.JourneyNextStart;

public interface SchedulerService {

    /**
     * add addJobList
     *
     * @param
     * @return
     */
    public JourneyNextStart addJobs(JourneyInfo journeyInfo);

    /**
     * add modifyJobList
     *
     * @param
     * @return
     */
    public JourneyNextStart modifyJob(String journeyId, JourneyInfo journeyInfo);

    /**
     * autoStartJobs
     *
     * @return
     */
    public void autoStartJobs();

    /**
     * autoStopJobs
     *
     * @return
     */
    public void autoStopJobs();

    /**
     * manualStartJobs
     *
     * @return
     */
    public void manualStartJobs(String journeyId);

    /**
     * manualStopJobs
     *
     * @return
     */
    public void manualStopJobs(String journeyId);

    /**
     * add deleteJobList
     *
     * @param
     * @return
     */
    public void deleteJobs(String journeyId);

    /**
     * add queryJobExeRecspageList
     *
     * @param
     * @return
     */
    public JourneyLogRes queryJobExeRecs(String journeyId, int page, int size, int status, String filterTime);

    /**
     * queryJobNextStart
     *
     * @return
     */
    public JourneyNextStart queryJobNextStart(String journeyId);

}
