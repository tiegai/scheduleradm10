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
    JourneyNextStart addJobs(JourneyInfo journeyInfo, String userId, String userName);

    /**
     * add modifyJobList
     *
     * @param
     * @return
     */
    JourneyNextStart modifyJob(String journeyId, JourneyInfo journeyInfo, String userId, String userName);

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
    void deleteJobs(String journeyId, String userId, String userName);

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
    JourneyNextStart queryJobNextStart(String journeyId, String userId, String userName);

}
