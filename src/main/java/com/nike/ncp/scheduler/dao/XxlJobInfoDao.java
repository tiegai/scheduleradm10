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
    List<XxlJobInfo> pageList(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("jobGroup") int jobGroup, @Param("triggerStatus") int triggerStatus, @Param("jobDesc") String jobDesc, @Param("executorHandler") String executorHandler, @Param("author") String author);

    int pageListCount(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("jobGroup") int jobGroup, @Param("triggerStatus") int triggerStatus, @Param("jobDesc") String jobDesc, @Param("executorHandler") String executorHandler, @Param("author") String author);

    int save(XxlJobInfo info);

    XxlJobInfo loadById(@Param("id") int id);

    int update(XxlJobInfo xxlJobInfo);

    int delete(@Param("id") long id);

    List<XxlJobInfo> getJobsByGroup(@Param("jobGroup") int jobGroup);

    int findAllCount();

    List<XxlJobInfo> scheduleJobQuery(@Param("maxNextTime") long maxNextTime, @Param("pagesize") int pagesize);

    int scheduleUpdate(XxlJobInfo xxlJobInfo);

    int autoStartJobs();

    int autoStopJobs();

    int manualStartJobs(@Param("journeyId") String journeyId);

    int manualStopJobs(@Param("journeyId") String journeyId);

    List<String> getCronByJourneyId(@Param("journeyId") String journeyId);

    int deleteByJourneyId(@Param("journeyId") String journeyId);

    List<XxlJobInfo> getJobsByJourneyId(@Param("journeyId") String journeyId);

    /*int deleteByIdsAndIdGroup(@Param("ids") List<Integer> ids,@Param("idGroup") int idGroup);*/
}
