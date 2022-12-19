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

	public int save(XxlJobLogReport xxlJobLogReport);

	public int update(XxlJobLogReport xxlJobLogReport);

	public List<XxlJobLogReport> queryLogReport(@Param("triggerDayFrom") Date triggerDayFrom,
												@Param("triggerDayTo") Date triggerDayTo);

	public XxlJobLogReport queryLogReportTotal();

}
