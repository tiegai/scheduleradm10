package com.nike.ncp.scheduler.service;


import com.nike.ncp.scheduler.core.model.XxlJobInfo;
import com.nike.ncp.scheduler.common.biz.model.ReturnT;

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
	Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author);

	/**
	 * add job
	 *
	 * @param jobInfo
	 * @return
	 */
	ReturnT<String> add(XxlJobInfo jobInfo);

	/**
	 * update job
	 *
	 * @param jobInfo
	 * @return
	 */
	ReturnT<String> update(XxlJobInfo jobInfo);

	/**
	 * remove job
	 * 	 *
	 * @param id
	 * @return
	 */
	ReturnT<String> remove(int id);

	/**
	 * start job
	 *
	 * @param id
	 * @return
	 */
	ReturnT<String> start(int id);

	/**
	 * stop job
	 *
	 * @param id
	 * @return
	 */
	ReturnT<String> stop(int id);

	/**
	 * dashboard info
	 *
	 * @return
	 */
	Map<String,Object> dashboardInfo();

	/**
	 * chart info
	 *
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);

}
