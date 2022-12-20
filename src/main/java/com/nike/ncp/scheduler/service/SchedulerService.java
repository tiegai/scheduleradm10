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
    public JourneyNextStart modifyJob(Integer journeyId, JourneyInfo journeyInfo);

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
    public void manualStartJobs(Integer journeyId);

    /**
     * manualStopJobs
     *
     * @return
     */
    public void manualStopJobs(Integer journeyId);

    /**
     * add deleteJobList
     *
     * @param
     * @return
     */
    public void deleteJobs(Integer journeyId);

    /**
     * add queryJobExeRecspageList
     *
     * @param
     * @return
     */
    public JourneyLogRes queryJobExeRecs(Integer journeyId, int page, int size, int status, String filterTime);

    /**
     * queryJobNextStart
     *
     * @return
     */
    public JourneyNextStart queryJobNextStart(Integer journeyId);

}
