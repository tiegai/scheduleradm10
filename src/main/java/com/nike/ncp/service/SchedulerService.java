package com.nike.ncp.service;

import com.nike.ncp.common.biz.model.ReturnT;
import com.nike.ncp.core.model.JourneyInfo;
import com.nike.ncp.core.model.JourneyNextStart;
import com.nike.ncp.core.model.JourneyLog;

import java.util.Map;

public interface SchedulerService {

    /**
     * add addJobList
     *
     * @param nikeJobInfoRequest
     * @return
     */
    public JourneyInfo addJobs(JourneyInfo journeyInfo);

    /**
     * add modifyJobList
     *
     * @param
     * @return
     */
    public JourneyInfo modifyJob(Integer journeyId, JourneyInfo journeyInfo);

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
    public JourneyLog queryJobExeRecs(Integer journeyId,int page, int size, int status,String filterTime);

    /**
     * queryJobNextStart
     *
     * @return
     */
    public JourneyNextStart queryJobNextStart(Integer journeyId);

}
