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
    JourneyNextStart addJobs(JourneyInfo journeyInfo);

    /**
     * add modifyJobList
     *
     * @param
     * @return
     */
    JourneyNextStart modifyJob(String journeyId, JourneyInfo journeyInfo);

    /**
     * autoStartJobs
     *
     * @return
     */
    void autoStartJobs();

    /**
     * autoStopJobs
     *
     * @return
     */
    void autoStopJobs();

    /**
     * manualStartJobs
     *
     * @return
     */
    void manualStartJobs(String journeyId, String userId, String userName);

    /**
     * manualStopJobs
     *
     * @return
     */
    void manualStopJobs(String journeyId, String userId, String userName);

    /**
     * add deleteJobList
     *
     * @param
     * @return
     */
    void deleteJobs(String journeyId);

    /**
     * add queryJobExeRecspageList
     *
     * @param
     * @return
     */
    JourneyLogRes queryJobExeRecs(String journeyId, int page, int size, int status, String filterTime);

    /**
     * queryJobNextStart
     *
     * @return
     */
    JourneyNextStart queryJobNextStart(String journeyId);

}
