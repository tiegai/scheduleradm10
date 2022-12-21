package com.nike.ncp.scheduler.service.impl;

import com.nike.ncp.scheduler.common.util.DateUtil;
import com.nike.ncp.scheduler.core.constant.ConCollections;
import com.nike.ncp.scheduler.core.util.CronUtil;
import com.nike.ncp.scheduler.core.util.UtcLocalDateUtil;
import com.nike.ncp.scheduler.dao.XxlJobInfoDao;
import com.nike.ncp.scheduler.dao.XxlJobLogDao;
import com.nike.ncp.scheduler.dao.XxlJobLogGlueDao;
import com.nike.ncp.scheduler.core.model.*;
import com.nike.ncp.scheduler.exception.ApiExceptions;
import com.nike.ncp.scheduler.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private static Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Value("${ncp.engine.url}")
    private String engineUrl;


    @Value("${ncp.scheduler.executor.job.group}")
    private Integer jobGroup;

    @Resource
    private XxlJobInfoDao xxlJobInfoDao;

    @Resource
    public XxlJobLogDao xxlJobLogDao;

    @Resource
    private XxlJobLogGlueDao xxlJobLogGlueDao;

    @Override
    @Transactional
    public JourneyNextStart addJobs(JourneyInfo journeyInfo) {
        // add in db
        XxlJobInfo jobInfo = new XxlJobInfo();
        String journeyId = journeyInfo.getJourneyId();
        jobInfo.setJourneyId(journeyId);
        jobInfo.setJobGroup(jobGroup);
        jobInfo.setJobDesc(journeyInfo.getDescription());
        jobInfo.setAddTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getCreatedTime())));
        jobInfo.setUpdateTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getModifiedTime())));
        jobInfo.setGlueUpdatetime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getModifiedTime())));
        String beginStr = journeyInfo.getPeriodicBegin();
        String endStr = journeyInfo.getPeriodicEnd();
        jobInfo.setJourneyStartTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr)));
        jobInfo.setJourneyEndTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(endStr)));
        jobInfo.setAuthor(ConCollections.AUTHOR);
        jobInfo.setScheduleType(ConCollections.SCHEDULE_TYPE);
        jobInfo.setGlueType(ConCollections.GLUE_TYPE);
        jobInfo.setExecutorRouteStrategy(ConCollections.EXECUTOR_ROUTE_STRATEGY);
        jobInfo.setMisfireStrategy(ConCollections.MISFIRE_STRATEGY);
        jobInfo.setExecutorBlockStrategy(ConCollections.EXECUTOR_BLOCK_STRATEGY);
        jobInfo.setExecutorHandler(ConCollections.EXECUTOR_HANDLER);
        jobInfo.setExecutorParam(engineUrl+ConCollections.ENGINE_URL_PARAMS+journeyId);
        String[] times = null;
        List<Date> timesList = new ArrayList<>();
        //once
        if(journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)){
            if(journeyInfo.getNextStartTime()==null){
                throw ApiExceptions.invalidRequest();
            }
        }else{ // not once
            times = journeyInfo.getPeriodicTimes().split(",");
            if(times.length<1){
                throw ApiExceptions.invalidRequest();
            }
            for(int i=0;i<times.length;i++){
                timesList.add(UtcLocalDateUtil.strToTime(times[i]+":00"));
            }
        }
        //正序排列
		/*Collections.sort(timesList);
		// 校验时间点相差不能小于5秒
		Date beginTime = UtcDateUtil.strToTime(UtcDateUtil.uctToLocalStr(beginStr).split("\\s+")[1]);
		Date endTime = UtcDateUtil.strToTime(UtcDateUtil.uctToLocalStr(endStr).split("\\s+")[1]);

		if(beginTime.compareTo(timesList.get(0)) > 0){
			return new ReturnT<String>(ReturnT.FAIL_CODE,"最早一次启动时间点必须大于开始时间点5秒");
		}
		if(endTime.compareTo(timesList.get(timesList.size()-1)) < 0){
			return new ReturnT<String>(ReturnT.FAIL_CODE,"最晚一次启动时间点必须小于结束时间点5秒");
		}*/

        // 根据多个触发时间，创建多个job
        //String returnTimes = "";
//		List<String> returnTimesList = new ArrayList<>();
        if(journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)){
            String nextStart = UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getNextStartTime());
            Integer year = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[0]);
            Integer month = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[1]);
            Integer day = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[2]);
            Integer hour = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[0]);
            Integer minute = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[1]);
            Integer second = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[2]);
            //0 40 17 2 12 ? 2022-2022
            jobInfo.setScheduleConf(second+" "+minute+" "+hour+" "+day+" "+month+" "+"?"+" "+year+"-"+year);
            xxlJobInfoDao.save(jobInfo);
        }else if(journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_DAILY)){
            for(int i = 0; i < times.length; i++){
                String[] timesStr = times[i].split(":");
                jobInfo.setScheduleConf(Integer.parseInt("0")+" "+Integer.parseInt(timesStr[1])+" "+Integer.parseInt(timesStr[0])+" "+"* * ?");
                xxlJobInfoDao.save(jobInfo);
//				String id = String.valueOf(jobInfo.getId());
//				returnTimesList.add(times[i]);
            }
			/*if(returnTimesList.size()>1){
				returnTimes = returnTimesList.get(0);
				for(int k=1; k<returnTimesList.size();k++){
					returnTimes = returnTimes+"&"+returnTimesList.get(k);
				}
			}else {
				returnTimes = returnTimesList.get(0);
			}*/
        }else if(journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_WEEKLY)){
            for(int i = 0; i < times.length; i++){
                String[] timesStr = times[i].split(":");
                String week = journeyInfo.getPeriodicValues();
                jobInfo.setScheduleConf(Integer.parseInt("0")+" "+Integer.parseInt(timesStr[1])+" "+Integer.parseInt(timesStr[0])+" "+"? *"+" "+week);
                xxlJobInfoDao.save(jobInfo);
//				String id = String.valueOf(jobInfo.getId());
//				returnTimesList.add(id+"#"+times[i]);
            }
			/*if(returnTimesList.size()>1){
				returnTimes = returnTimesList.get(0);
				for(int k=1; k<returnTimesList.size();k++){
					returnTimes = returnTimes+"&"+returnTimesList.get(k);
				}
			}else {
				returnTimes = returnTimesList.get(0);
			}*/
        }else if(journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_MONTHLY)){
            for(int i = 0; i < times.length; i++){
                String[] timesStr = times[i].split(":");
                String day = journeyInfo.getPeriodicValues();
                jobInfo.setScheduleConf(Integer.parseInt("0")+" "+Integer.parseInt(timesStr[1])+" "+Integer.parseInt(timesStr[0])+" "+day+" "+"* ?");
                xxlJobInfoDao.save(jobInfo);
				/*String id = String.valueOf(jobInfo.getId());
				returnTimesList.add(id+"#"+times[i]);*/
            }
			/*if(returnTimesList.size()>1){
				returnTimes = returnTimesList.get(0);
				for(int k=1; k<returnTimesList.size();k++){
					returnTimes = returnTimes+"&"+returnTimesList.get(k);
				}
			}else {
				returnTimes = returnTimesList.get(0);
			}*/
        }

        JourneyNextStart response = new JourneyNextStart();
        response.setJourneyId(journeyId);
        // 校验是否全成功创建所有job,并返回nextStart
        List<String> cronList = xxlJobInfoDao.getCronByJourneyId(journeyId);
        if(journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)){
            if(cronList.size() != 1){
                throw ApiExceptions.itemNotFound();
            }
            response.setNextStartTime(journeyInfo.getNextStartTime());
        }else{
            if(cronList.size() != timesList.size()){
                throw ApiExceptions.itemNotFound();
            }
            response.setNextStartTime(CronUtil.cronListNextStart(cronList, UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr))));
        }
        return response;
    }


    @Override
    @Transactional
    public JourneyNextStart modifyJob(String journeyId, JourneyInfo journeyInfo) {
        xxlJobInfoDao.deleteByJourneyId(journeyId);
        journeyInfo.setJourneyId(journeyId);
        JourneyNextStart journeyNextStart = addJobs(journeyInfo);
        return journeyNextStart;
    }


    @Override
    public void autoStartJobs(){
        xxlJobInfoDao.autoStartJobs();
    }

    @Override
    public void autoStopJobs(){
        xxlJobInfoDao.autoStopJobs();
    }

    @Override
    public void manualStartJobs(String journeyId){
        xxlJobInfoDao.manualStartJobs(journeyId);
    }

    @Override
    public void manualStopJobs(String journeyId){
        xxlJobInfoDao.manualStopJobs(journeyId);
    }

    @Override
    @Transactional
    public void deleteJobs(String journeyId) {
        List<String> cronList = xxlJobInfoDao.getCronByJourneyId(journeyId);
        if (cronList.size() < 0) {
            return;
        }
        xxlJobInfoDao.deleteByJourneyId(journeyId);
        xxlJobLogDao.deleteByJourneyId(journeyId);
        xxlJobLogGlueDao.deleteByJourneyId(journeyId);
    }

    @Override
    public JourneyLogRes queryJobExeRecs(String journeyId, int page, int size, int status, String filterTime) {

        Date triggerTimeStart = null;
        Date triggerTimeEnd = null;
        if (filterTime!=null && filterTime.trim().length()>0) {
            String[] temp = filterTime.split(" - ");
            if (temp.length == 2) {
                triggerTimeStart = DateUtil.parseDateTime(temp[0]);
                triggerTimeEnd = DateUtil.parseDateTime(temp[1]);
            }
        }
        // page query
        List<XxlJobLog> list = xxlJobLogDao.recsPageList(page, size, journeyId, triggerTimeStart, triggerTimeEnd, status);
        int list_count = xxlJobLogDao.recsPageListCount(page, size, journeyId, triggerTimeStart, triggerTimeEnd, status);

        JourneyLogRes journeyLogRes = new JourneyLogRes();
        JourneyLogPage journeyLogPage = new JourneyLogPage();
        journeyLogPage.setTotal(list_count);
        journeyLogPage.setSize(size);
        journeyLogPage.setCurrent(page);
        journeyLogPage.setPages(page);
        journeyLogRes.setData(list);
        journeyLogRes.setPage(journeyLogPage);
        return journeyLogRes;
    }

    @Override
    public JourneyNextStart queryJobNextStart(String journeyId){
        JourneyNextStart response = new JourneyNextStart();
        response.setJourneyId(journeyId);
        List<XxlJobInfo> jobInfoList = xxlJobInfoDao.getJobsByJourneyId(journeyId);
        if(jobInfoList == null || jobInfoList.size() < 1){
            throw ApiExceptions.itemNotFound();
        }
        Date begin = jobInfoList.get(0).getJourneyStartTime();
        Date end = jobInfoList.get(0).getJourneyEndTime();
        List<String> cronList = new ArrayList<>();
        Date start = new Date();
        if(start.before(begin)){
            start = begin;
        }
        for(int i=0;i<jobInfoList.size();i++){
            cronList.add(jobInfoList.get(i).getScheduleConf());
        }
        String nextStart = CronUtil.cronListNextStart(cronList,start);
        if(end.before(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(nextStart)))){
            response.setNextStartTime("no next start");
        }else{
            response.setNextStartTime(nextStart);
        }
        return response;
    }
}
