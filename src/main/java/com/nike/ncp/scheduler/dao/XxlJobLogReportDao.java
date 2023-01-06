package com.nike.ncp.scheduler.dao;

import com.nike.ncp.scheduler.core.model.XxlJobLogReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * job log
 */
@Mapper
public interface XxlJobLogReportDao {

	int save(XxlJobLogReport xxlJobLogReport);

	int update(XxlJobLogReport xxlJobLogReport);

	List<XxlJobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
												@Param("triggerDayTo") Date triggerDayTo);

	XxlJobLogReport queryLogReportTotal();

}
