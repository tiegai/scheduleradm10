package com.nike.ncp.scheduler.dao;

import com.nike.ncp.scheduler.core.model.XxlJobInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * job info
 */
@Mapper
public interface XxlJobInfoDao {

	public List<XxlJobInfo> pageList(@Param("offset") int offset,
                                     @Param("pagesize") int pagesize,
                                     @Param("jobGroup") int jobGroup,
                                     @Param("triggerStatus") int triggerStatus,
                                     @Param("jobDesc") String jobDesc,
                                     @Param("executorHandler") String executorHandler,
                                     @Param("author") String author);
	public int pageListCount(@Param("offset") int offset,
							 @Param("pagesize") int pagesize,
							 @Param("jobGroup") int jobGroup,
							 @Param("triggerStatus") int triggerStatus,
							 @Param("jobDesc") String jobDesc,
							 @Param("executorHandler") String executorHandler,
							 @Param("author") String author);
	
	public int save(XxlJobInfo info);

	public XxlJobInfo loadById(@Param("id") int id);
	
	public int update(XxlJobInfo xxlJobInfo);
	
	public int delete(@Param("id") long id);

	public List<XxlJobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

	public int findAllCount();

	public List<XxlJobInfo> scheduleJobQuery(@Param("maxNextTime") long maxNextTime, @Param("pagesize") int pagesize );

	public int scheduleUpdate(XxlJobInfo xxlJobInfo);

	public int autoStartJobs();

	public int autoStopJobs();

	public int manualStartJobs(@Param("journeyId") String journeyId);

	public int manualStopJobs(@Param("journeyId") String journeyId);

	public List<String> getCronByJourneyId(@Param("journeyId") String journeyId);

	public int deleteByJourneyId(@Param("journeyId") String journeyId);

	public List<XxlJobInfo> getJobsByJourneyId(@Param("journeyId") String journeyId);

	/*public int deleteByIdsAndIdGroup(@Param("ids") List<Integer> ids,@Param("idGroup") int idGroup);*/
}
