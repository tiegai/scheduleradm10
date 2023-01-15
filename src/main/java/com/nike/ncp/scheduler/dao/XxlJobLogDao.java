package com.nike.ncp.scheduler.dao;

import com.nike.ncp.scheduler.core.model.XxlJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * job log
 */
@Mapper
public interface XxlJobLogDao {

    // exist jobId not use jobGroup, not exist use jobGroup
    List<XxlJobLog> pageList(@Param("offsetList") int offset,
                             @Param("pageSizeList") int pageSize,
                             @Param("jobGroup") int jobGroup,
                             @Param("jobIdList") int jobId,
                             @Param("triggerTimeStartList") Date triggerTimeStart,
                             @Param("triggerTimeEndList") Date triggerTimeEnd,
                             @Param("logStatusList") int logStatus);

    int pageListCount(@Param("offsetCount") int offset,
                      @Param("pageSizeCount") int pageSize,
                      @Param("jobGroup") int jobGroup,
                      @Param("jobIdCount") int jobId,
                      @Param("triggerTimeStartCount") Date triggerTimeStart,
                      @Param("triggerTimeEndCount") Date triggerTimeEnd,
                      @Param("logStatusCount") int logStatus);

    XxlJobLog load(@Param("id") long id);

    long save(XxlJobLog xxlJobLog);

    int updateTriggerInfo(XxlJobLog xxlJobLog);

    int updateHandleInfo(XxlJobLog xxlJobLog);

    int delete(@Param("jobIdDel") int jobId);

    Map<String, Object> findLogReport(@Param("from") Date from,
                                      @Param("to") Date to);

    List<Long> findClearLogIds(@Param("jobGroup") int jobGroup,
                               @Param("jobIdClear") int jobId,
                               @Param("clearBeforeTime") Date clearBeforeTime,
                               @Param("clearBeforeNum") int clearBeforeNum,
                               @Param("pageSizeClear") int pageSize);

    int clearLog(@Param("logIds") List<Long> logIds);

    List<Long> findFailJobLogIds(@Param("pageSizeFail") int pageSize);

    int updateAlarmStatus(@Param("logId") long logId,
                          @Param("oldAlarmStatus") int oldAlarmStatus,
                          @Param("newAlarmStatus") int newAlarmStatus);

    List<Long> findLostJobIds(@Param("losedTime") Date losedTime);

    int deleteByJourneyId(@Param("journeyId") String journeyId);

    List<XxlJobLog> recsPageList(@Param("offsetRecList") int offset,
                                 @Param("pageSizeRecList") int pageSize,
                                 //@Param("jobGroup") int jobGroup,
                                 @Param("journeyId") String journeyId,
                                 @Param("triggerTimeStartRecList") Date triggerTimeStart,
                                 @Param("triggerTimeEndRecList") Date triggerTimeEnd,
                                 @Param("logStatusRecList") int logStatus);

    int recsPageListCount(@Param("offsetRecCount") int offset,
                          @Param("pageSizeRecCount") int pageSize,
                          //@Param("jobGroup") int jobGroup,
                          @Param("journeyId") String journeyId,
                          @Param("triggerTimeStartRecCount") Date triggerTimeStart,
                          @Param("triggerTimeEndRecCount") Date triggerTimeEnd,
                          @Param("logStatusRecCount") int logStatus);

}
