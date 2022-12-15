package com.nike.springboottemplate.service;


import com.nike.springboottemplate.core.model.NikeJobInfoRequest;
import com.nike.springboottemplate.core.model.NikeJobLogRequest;
import com.nike.springboottemplate.core.model.XxlJobInfo;
import com.nike.springboottemplate.common.biz.model.ReturnT;

import java.util.Date;
import java.util.Map;

/**
 * core job action for xxl-job
 */
public interface 	XxlJobService {

	/**
	 * page list
	 *
	 * @param start
	 * @param length
	 * @param jobGroup
	 * @param jobDesc
	 * @param executorHandler
	 * @param author
	 * @return
	 */
	public Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author);

	/**
	 * add job
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> add(XxlJobInfo jobInfo);

	/**
	 * update job
	 *
	 * @param jobInfo
	 * @return
	 */
	public ReturnT<String> update(XxlJobInfo jobInfo);

	/**
	 * remove job
	 * 	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> remove(int id);

	/**
	 * start job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> start(int id);

	/**
	 * stop job
	 *
	 * @param id
	 * @return
	 */
	public ReturnT<String> stop(int id);

	/**
	 * dashboard info
	 *
	 * @return
	 */
	public Map<String,Object> dashboardInfo();

	/**
	 * chart info
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);


	/**
	 * add addJobList
	 *
	 * @param nikeJobInfoRequest
	 * @return
	 */
	public ReturnT<String> addJobList(NikeJobInfoRequest nikeJobInfoRequest);

	/**
	 * add deleteJobList
	 *
	 * @param
	 * @return
	 */
	public ReturnT<String> deleteJobList(NikeJobInfoRequest nikeJobInfoRequest);

	/**
	 * add modifyJobList
	 *
	 * @param
	 * @return
	 */
	public ReturnT<String> modifyJobList(NikeJobInfoRequest nikeJobInfoRequest);

	/**
	 * add queryJobExeRecs
	 *
	 * @param
	 * @return
	 */
	public ReturnT<String> queryJobExeRecs(NikeJobInfoRequest nikeJobInfoRequest);

	/**
	 * add queryJobExeRecspageList
	 *
	 * @param
	 * @return
	 */
	public ReturnT<String> queryJobExeRecsPageList(NikeJobLogRequest nikeJobLogRequest);

	/**
	 * autoStartJobs
	 *
	 * @return
	 */
	public ReturnT<String> autoStartJobs();

	/**
	 * autoStopJobs
	 *
	 * @return
	 */
	public ReturnT<String> autoStopJobs();

	/**
	 * manualStartJobs
	 *
	 * @return
	 */
	public ReturnT<String> manualStartJobs(NikeJobInfoRequest nikeJobInfoRequest);

	/**
	 * manualStopJobs
	 *
	 * @return
	 */
	public ReturnT<String> manualStopJobs(NikeJobInfoRequest nikeJobInfoRequest);

	/**
	 * queryJobNextStart
	 *
	 * @return
	 */
	public ReturnT<String> queryJobNextStart(NikeJobInfoRequest nikeJobInfoRequest);

}
