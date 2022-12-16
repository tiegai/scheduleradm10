package com.nike.ncp.service.impl;

import com.google.gson.Gson;
import com.nike.ncp.common.biz.model.ReturnT;
import com.nike.ncp.common.enums.ExecutorBlockStrategyEnum;
import com.nike.ncp.common.glue.GlueTypeEnum;
import com.nike.ncp.common.util.DateUtil;
import com.nike.ncp.core.constant.ConCollections;
import com.nike.ncp.core.cron.CronExpression;
import com.nike.ncp.core.model.*;
import com.nike.ncp.core.route.ExecutorRouteStrategyEnum;
import com.nike.ncp.core.scheduler.ScheduleTypeEnum;
import com.nike.ncp.core.thread.JobScheduleHelper;
import com.nike.ncp.dao.*;
import com.nike.ncp.service.XxlJobService;
import com.nike.ncp.core.model.*;
import com.nike.ncp.core.scheduler.MisfireStrategyEnum;
import com.nike.ncp.core.util.CronUtil;
import com.nike.ncp.core.util.I18nUtil;
import com.nike.ncp.core.util.UtcLocalDateUtil;
import com.nike.ncp.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;

/**
 * core job action for xxl-job
 */
@Service
public class XxlJobServiceImpl implements XxlJobService {
	private static Logger logger = LoggerFactory.getLogger(XxlJobServiceImpl.class);

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;
	@Resource
	private XxlJobInfoDao xxlJobInfoDao;
	@Resource
	public XxlJobLogDao xxlJobLogDao;
	@Resource
	private XxlJobLogGlueDao xxlJobLogGlueDao;
	@Resource
	private XxlJobLogReportDao xxlJobLogReportDao;
	
	@Override
	public Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {

		// page list
		List<XxlJobInfo> list = xxlJobInfoDao.pageList(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
		int list_count = xxlJobInfoDao.pageListCount(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
		
		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
	    maps.put("recordsTotal", list_count);		// 总记录数
	    maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
	    maps.put("data", list);  					// 分页列表
		return maps;
	}

	@Override
	public ReturnT<String> add(XxlJobInfo jobInfo) {

		// valid base
		XxlJobGroup group = xxlJobGroupDao.load(jobInfo.getJobGroup());
		if (group == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
		}
		if (jobInfo.getJobDesc()==null || jobInfo.getJobDesc().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_jobdesc")) );
		}
		if (jobInfo.getAuthor()==null || jobInfo.getAuthor().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_author")) );
		}

		// valid trigger
		ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(jobInfo.getScheduleType(), null);
		if (scheduleTypeEnum == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
		}
		if (scheduleTypeEnum == ScheduleTypeEnum.CRON) {
			if (jobInfo.getScheduleConf()==null || !CronExpression.isValidExpression(jobInfo.getScheduleConf())) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, "Cron"+I18nUtil.getString("system_unvalid"));
			}
		} else if (scheduleTypeEnum == ScheduleTypeEnum.FIX_RATE/* || scheduleTypeEnum == ScheduleTypeEnum.FIX_DELAY*/) {
			if (jobInfo.getScheduleConf() == null) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")) );
			}
			try {
				int fixSecond = Integer.valueOf(jobInfo.getScheduleConf());
				if (fixSecond < 1) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
				}
			} catch (Exception e) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
			}
		}

		// valid job
		if (GlueTypeEnum.match(jobInfo.getGlueType()) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_gluetype")+I18nUtil.getString("system_unvalid")) );
		}
		if (GlueTypeEnum.BEAN==GlueTypeEnum.match(jobInfo.getGlueType()) && (jobInfo.getExecutorHandler()==null || jobInfo.getExecutorHandler().trim().length()==0) ) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+"JobHandler") );
		}
		// 》fix "\r" in shell
		if (GlueTypeEnum.GLUE_SHELL==GlueTypeEnum.match(jobInfo.getGlueType()) && jobInfo.getGlueSource()!=null) {
			jobInfo.setGlueSource(jobInfo.getGlueSource().replaceAll("\r", ""));
		}

		// valid advanced
		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy")+I18nUtil.getString("system_unvalid")) );
		}
		if (MisfireStrategyEnum.match(jobInfo.getMisfireStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("misfire_strategy")+I18nUtil.getString("system_unvalid")) );
		}
		if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy")+I18nUtil.getString("system_unvalid")) );
		}

		// 》ChildJobId valid
		if (jobInfo.getChildJobId()!=null && jobInfo.getChildJobId().trim().length()>0) {
			String[] childJobIds = jobInfo.getChildJobId().split(",");
			for (String childJobIdItem: childJobIds) {
				if (childJobIdItem!=null && childJobIdItem.trim().length()>0 && isNumeric(childJobIdItem)) {
					XxlJobInfo childJobInfo = xxlJobInfoDao.loadById(Integer.parseInt(childJobIdItem));
					if (childJobInfo==null) {
						return new ReturnT<String>(ReturnT.FAIL_CODE,
								MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId")+"({0})"+I18nUtil.getString("system_not_found")), childJobIdItem));
					}
				} else {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId")+"({0})"+I18nUtil.getString("system_unvalid")), childJobIdItem));
				}
			}

			// join , avoid "xxx,,"
			String temp = "";
			for (String item:childJobIds) {
				temp += item + ",";
			}
			temp = temp.substring(0, temp.length()-1);

			jobInfo.setChildJobId(temp);
		}

		// add in db
		jobInfo.setAddTime(new Date());
		jobInfo.setUpdateTime(new Date());
		jobInfo.setGlueUpdatetime(new Date());
		xxlJobInfoDao.save(jobInfo);
		if (jobInfo.getId() < 1) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
		}

		return new ReturnT<String>(String.valueOf(jobInfo.getId()));
	}

	private boolean isNumeric(String str){
		try {
			int result = Integer.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public ReturnT<String> update(XxlJobInfo jobInfo) {

		// valid base
		if (jobInfo.getJobDesc()==null || jobInfo.getJobDesc().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_jobdesc")) );
		}
		if (jobInfo.getAuthor()==null || jobInfo.getAuthor().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_author")) );
		}

		// valid trigger
		ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(jobInfo.getScheduleType(), null);
		if (scheduleTypeEnum == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
		}
		if (scheduleTypeEnum == ScheduleTypeEnum.CRON) {
			if (jobInfo.getScheduleConf()==null || !CronExpression.isValidExpression(jobInfo.getScheduleConf())) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, "Cron"+I18nUtil.getString("system_unvalid") );
			}
		} else if (scheduleTypeEnum == ScheduleTypeEnum.FIX_RATE /*|| scheduleTypeEnum == ScheduleTypeEnum.FIX_DELAY*/) {
			if (jobInfo.getScheduleConf() == null) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
			}
			try {
				int fixSecond = Integer.valueOf(jobInfo.getScheduleConf());
				if (fixSecond < 1) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
				}
			} catch (Exception e) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
			}
		}

		// valid advanced
		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy")+I18nUtil.getString("system_unvalid")) );
		}
		if (MisfireStrategyEnum.match(jobInfo.getMisfireStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("misfire_strategy")+I18nUtil.getString("system_unvalid")) );
		}
		if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy")+I18nUtil.getString("system_unvalid")) );
		}

		// 》ChildJobId valid
		if (jobInfo.getChildJobId()!=null && jobInfo.getChildJobId().trim().length()>0) {
			String[] childJobIds = jobInfo.getChildJobId().split(",");
			for (String childJobIdItem: childJobIds) {
				if (childJobIdItem!=null && childJobIdItem.trim().length()>0 && isNumeric(childJobIdItem)) {
					XxlJobInfo childJobInfo = xxlJobInfoDao.loadById(Integer.parseInt(childJobIdItem));
					if (childJobInfo==null) {
						return new ReturnT<String>(ReturnT.FAIL_CODE,
								MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId")+"({0})"+I18nUtil.getString("system_not_found")), childJobIdItem));
					}
				} else {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format((I18nUtil.getString("jobinfo_field_childJobId")+"({0})"+I18nUtil.getString("system_unvalid")), childJobIdItem));
				}
			}

			// join , avoid "xxx,,"
			String temp = "";
			for (String item:childJobIds) {
				temp += item + ",";
			}
			temp = temp.substring(0, temp.length()-1);

			jobInfo.setChildJobId(temp);
		}

		// group valid
		XxlJobGroup jobGroup = xxlJobGroupDao.load(jobInfo.getJobGroup());
		if (jobGroup == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_jobgroup")+I18nUtil.getString("system_unvalid")) );
		}

		// stage job info
		XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(jobInfo.getId());
		if (exists_jobInfo == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_id")+I18nUtil.getString("system_not_found")) );
		}

		// next trigger time (5s后生效，避开预读周期)
		long nextTriggerTime = exists_jobInfo.getTriggerNextTime();
		boolean scheduleDataNotChanged = jobInfo.getScheduleType().equals(exists_jobInfo.getScheduleType()) && jobInfo.getScheduleConf().equals(exists_jobInfo.getScheduleConf());
		if (exists_jobInfo.getTriggerStatus() == 1 && !scheduleDataNotChanged) {
			try {
				Date nextValidTime = JobScheduleHelper.generateNextValidTime(jobInfo, new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
				if (nextValidTime == null) {
					return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
				}
				nextTriggerTime = nextValidTime.getTime();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
			}
		}

		exists_jobInfo.setJobGroup(jobInfo.getJobGroup());
		exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
		exists_jobInfo.setAuthor(jobInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
		exists_jobInfo.setScheduleType(jobInfo.getScheduleType());
		exists_jobInfo.setScheduleConf(jobInfo.getScheduleConf());
		exists_jobInfo.setMisfireStrategy(jobInfo.getMisfireStrategy());
		exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
		exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		exists_jobInfo.setExecutorTimeout(jobInfo.getExecutorTimeout());
		exists_jobInfo.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
		exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
		exists_jobInfo.setTriggerNextTime(nextTriggerTime);

		exists_jobInfo.setUpdateTime(new Date());
        xxlJobInfoDao.update(exists_jobInfo);


		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> remove(int id) {
		XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
		if (xxlJobInfo == null) {
			return ReturnT.SUCCESS;
		}

		xxlJobInfoDao.delete(id);
		xxlJobLogDao.delete(id);
		xxlJobLogGlueDao.deleteByJobId(id);
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> start(int id) {
		XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);

		// valid
		ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(xxlJobInfo.getScheduleType(), ScheduleTypeEnum.NONE);
		if (ScheduleTypeEnum.NONE == scheduleTypeEnum) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type_none_limit_start")) );
		}

		// next trigger time (5s后生效，避开预读周期)
		long nextTriggerTime = 0;
		try {
			Date nextValidTime = JobScheduleHelper.generateNextValidTime(xxlJobInfo, new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
			if (nextValidTime == null) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
			}
			nextTriggerTime = nextValidTime.getTime();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) );
		}

		xxlJobInfo.setTriggerStatus(1);
		xxlJobInfo.setTriggerLastTime(0);
		xxlJobInfo.setTriggerNextTime(nextTriggerTime);

		xxlJobInfo.setUpdateTime(new Date());
		xxlJobInfoDao.update(xxlJobInfo);
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> stop(int id) {
        XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(id);

		xxlJobInfo.setTriggerStatus(2);
		xxlJobInfo.setTriggerLastTime(0);
		xxlJobInfo.setTriggerNextTime(0);

		xxlJobInfo.setUpdateTime(new Date());
		xxlJobInfoDao.update(xxlJobInfo);
		return ReturnT.SUCCESS;
	}

	@Override
	public Map<String, Object> dashboardInfo() {

		int jobInfoCount = xxlJobInfoDao.findAllCount();
		int jobLogCount = 0;
		int jobLogSuccessCount = 0;
		XxlJobLogReport xxlJobLogReport = xxlJobLogReportDao.queryLogReportTotal();
		if (xxlJobLogReport != null) {
			jobLogCount = xxlJobLogReport.getRunningCount() + xxlJobLogReport.getSucCount() + xxlJobLogReport.getFailCount();
			jobLogSuccessCount = xxlJobLogReport.getSucCount();
		}

		// executor count
		Set<String> executorAddressSet = new HashSet<String>();
		List<XxlJobGroup> groupList = xxlJobGroupDao.findAll();

		if (groupList!=null && !groupList.isEmpty()) {
			for (XxlJobGroup group: groupList) {
				if (group.getRegistryList()!=null && !group.getRegistryList().isEmpty()) {
					executorAddressSet.addAll(group.getRegistryList());
				}
			}
		}

		int executorCount = executorAddressSet.size();

		Map<String, Object> dashboardMap = new HashMap<String, Object>();
		dashboardMap.put("jobInfoCount", jobInfoCount);
		dashboardMap.put("jobLogCount", jobLogCount);
		dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
		dashboardMap.put("executorCount", executorCount);
		return dashboardMap;
	}

	@Override
	public ReturnT<Map<String, Object>> chartInfo(Date startDate, Date endDate) {

		// process
		List<String> triggerDayList = new ArrayList<String>();
		List<Integer> triggerDayCountRunningList = new ArrayList<Integer>();
		List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
		List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
		int triggerCountRunningTotal = 0;
		int triggerCountSucTotal = 0;
		int triggerCountFailTotal = 0;

		List<XxlJobLogReport> logReportList = xxlJobLogReportDao.queryLogReport(startDate, endDate);

		if (logReportList!=null && logReportList.size()>0) {
			for (XxlJobLogReport item: logReportList) {
				String day = DateUtil.formatDate(item.getTriggerDay());
				int triggerDayCountRunning = item.getRunningCount();
				int triggerDayCountSuc = item.getSucCount();
				int triggerDayCountFail = item.getFailCount();

				triggerDayList.add(day);
				triggerDayCountRunningList.add(triggerDayCountRunning);
				triggerDayCountSucList.add(triggerDayCountSuc);
				triggerDayCountFailList.add(triggerDayCountFail);

				triggerCountRunningTotal += triggerDayCountRunning;
				triggerCountSucTotal += triggerDayCountSuc;
				triggerCountFailTotal += triggerDayCountFail;
			}
		} else {
			for (int i = -6; i <= 0; i++) {
				triggerDayList.add(DateUtil.formatDate(DateUtil.addDays(new Date(), i)));
				triggerDayCountRunningList.add(0);
				triggerDayCountSucList.add(0);
				triggerDayCountFailList.add(0);
			}
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("triggerDayList", triggerDayList);
		result.put("triggerDayCountRunningList", triggerDayCountRunningList);
		result.put("triggerDayCountSucList", triggerDayCountSucList);
		result.put("triggerDayCountFailList", triggerDayCountFailList);

		result.put("triggerCountRunningTotal", triggerCountRunningTotal);
		result.put("triggerCountSucTotal", triggerCountSucTotal);
		result.put("triggerCountFailTotal", triggerCountFailTotal);

		return new ReturnT<Map<String, Object>>(result);
	}

	@Override
	@Transactional
	public ReturnT<String> addJobList(NikeJobInfoRequest nikeJobInfoRequest) {
		// add in db
		XxlJobInfo jobInfo = new XxlJobInfo();
		Integer idGroup = nikeJobInfoRequest.getJourneyId();
		jobInfo.setIdGroup(idGroup);
		jobInfo.setJobGroup(nikeJobInfoRequest.getCampaignId());
		jobInfo.setJobDesc(nikeJobInfoRequest.getDescription());
		jobInfo.setAddTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(nikeJobInfoRequest.getCreatedTime())));
		jobInfo.setUpdateTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(nikeJobInfoRequest.getModifiedTime())));
		jobInfo.setGlueUpdatetime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(nikeJobInfoRequest.getModifiedTime())));
		String beginStr = nikeJobInfoRequest.getPeriodicBegin();
		String endStr = nikeJobInfoRequest.getPeriodicEnd();
		jobInfo.setTriggerStartTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr)));
		jobInfo.setTriggerEndTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(endStr)));
		jobInfo.setAuthor(ConCollections.AUTHOR);
		jobInfo.setScheduleType(ConCollections.SCHEDULE_TYPE);
		jobInfo.setGlueType(ConCollections.GLUE_TYPE);
		jobInfo.setExecutorRouteStrategy(ConCollections.EXECUTOR_ROUTE_STRATEGY);
		jobInfo.setMisfireStrategy(ConCollections.MISFIRE_STRATEGY);
		jobInfo.setExecutorBlockStrategy(ConCollections.EXECUTOR_BLOCK_STRATEGY);
		jobInfo.setExecutorHandler(ConCollections.EXECUTOR_HANDLER);
		jobInfo.setExecutorParam(nikeJobInfoRequest.getJourneyAddress()+ConCollections.ENGINE_URL_PARAMS+idGroup);
		String[] times = null;
		List<Date> timesList = new ArrayList<>();
		//once
		if(nikeJobInfoRequest.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)){
			if(nikeJobInfoRequest.getNextStartTime()==null){
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
			}
		}else{ // not once
			times = nikeJobInfoRequest.getPeriodicTimes().split(",");
			if(times.length<1){
				return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
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
		if(nikeJobInfoRequest.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)){
			String nextStart = UtcLocalDateUtil.utcStrToLocalStr(nikeJobInfoRequest.getNextStartTime());
			Integer year = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[0]);
			Integer month = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[1]);
			Integer day = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[2]);
			Integer hour = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[0]);
			Integer minute = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[1]);
			Integer second = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[2]);
			//0 40 17 2 12 ? 2022-2022
			jobInfo.setScheduleConf(second+" "+minute+" "+hour+" "+day+" "+month+" "+"?"+" "+year+"-"+year);
			xxlJobInfoDao.save(jobInfo);
		}else if(nikeJobInfoRequest.getPeriodicType().equals(ConCollections.PERIODIC_DAILY)){
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
		}else if(nikeJobInfoRequest.getPeriodicType().equals(ConCollections.PERIODIC_WEEKLY)){
			for(int i = 0; i < times.length; i++){
				String[] timesStr = times[i].split(":");
				String week = nikeJobInfoRequest.getPeriodicValues();
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
		}else if(nikeJobInfoRequest.getPeriodicType().equals(ConCollections.PERIODIC_MONTHLY)){
			for(int i = 0; i < times.length; i++){
				String[] timesStr = times[i].split(":");
				String day = nikeJobInfoRequest.getPeriodicValues();
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

		NikeJobInfoResponse response = new NikeJobInfoResponse();
		response.setJourneyId(idGroup);
		// 校验是否全成功创建所有job,并返回nextStart
		List<String> cronList = xxlJobInfoDao.getCronByIdGroup(idGroup);
		if(nikeJobInfoRequest.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)){
			if(cronList.size() != 1){
				return new ReturnT<String>(ReturnT.FAIL_CODE,"addJobList is failed");
			}
			response.setNextStartTime(nikeJobInfoRequest.getNextStartTime());
		}else{
			if(cronList.size() != timesList.size()){
				return new ReturnT<String>(ReturnT.FAIL_CODE,"addJobList is failed");
			}
			response.setNextStartTime(CronUtil.cronListNextStart(cronList, UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr))));
		}

		/*nikeJobInfo.setTimes(returnTimes);
		JSONObject jsonObject = (JSONObject) JSONObject.toJSON(nikeJobInfo);
		nikeJobListEntity.setData(jsonObject.toString());
		JSONObject jsonObjectReturn = (JSONObject) JSONObject.toJSON(nikeJobListEntity);
		return new ReturnT<String>(jsonObjectReturn.toString());*/

		Gson gson = new Gson();
//		String listToJsonString = gson.toJson(cronList);
		String jsonString = gson.toJson(response);
		return new ReturnT<String>(jsonString);
	}

	@Override
	@Transactional
	public ReturnT<String> deleteJobList(NikeJobInfoRequest nikeJobInfoRequest) {
		Integer idGroup = nikeJobInfoRequest.getJourneyId();
		List<String> cronList = xxlJobInfoDao.getCronByIdGroup(idGroup);
		if (cronList.size() < 0) {
			return new ReturnT<String>("SUCCESS");
		}
		xxlJobInfoDao.deleteByIdGroup(idGroup);
		xxlJobLogDao.deleteByJobIdGroup(idGroup);
		xxlJobLogGlueDao.deleteByJobIdGroup(idGroup);
		return new ReturnT<String>("SUCCESS");
	}

	@Override
	@Transactional
	public ReturnT<String> modifyJobList(NikeJobInfoRequest nikeJobInfoRequest) {
		xxlJobInfoDao.deleteByIdGroup(nikeJobInfoRequest.getJourneyId());
		ReturnT<String> returnT = addJobList(nikeJobInfoRequest);
		return returnT;
	}

/*
	public ReturnT<String> modifyJobList(NikeJobListEntity nikeJobListEntity) {
		// add in db
		XxlJobInfo jobInfo = new XxlJobInfo();
		jobInfo.setAddTime(new Date());
		jobInfo.setUpdateTime(new Date());
		jobInfo.setGlueUpdatetime(new Date());

		jobInfo.setIdGroup(nikeJobListEntity.getIdGroup());
		jobInfo.setJobGroup(1);
		jobInfo.setJobDesc("Nike定时任务");
		jobInfo.setAuthor("Jerry");
		jobInfo.setScheduleType("CRON");
		jobInfo.setGlueType("BEAN");
		jobInfo.setExecutorRouteStrategy("FIRST");
		jobInfo.setMisfireStrategy("DO_NOTHING");
		jobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
		jobInfo.setExecutorHandler("httpJobHandler");
		jobInfo.setExecutorParam("url:http://localhost:8088/actor/postTest\\r\\nmethod:POST\\r\\ndata:params=jerry_add");
		//解析入参
		Integer idGroup = nikeJobListEntity.getIdGroup();
		jobInfo.setIdGroup(idGroup);
		NikeJobInfo nikeJobInfo =  JSONObject.parseObject(nikeJobListEntity.getData(), NikeJobInfo.class);
		String[] timesMayId = nikeJobInfo.getTimes().split("-");
		if(timesMayId.length<1){
			return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("jobinfo_field_add")+I18nUtil.getString("system_fail")) );
		}
		//检验时间修改是否符合规定
		List<Date> timesList = new ArrayList<>();
		Map<String,String> timesAndIdMap = new HashMap<>();
		//收集修改后的jobId
		List<Integer> timesIdList = new ArrayList<>();
		SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
		for(int i=0;i<timesMayId.length;i++){
			String timesTmp = timesMayId[i];
			try{
				if(timesTmp.contains("#")){
					timesList.add(formatTime.parse(timesTmp.split("#")[1]));
					timesAndIdMap.put(timesTmp.split("#")[0],timesTmp.split("#")[1]);
					timesIdList.add(Integer.parseInt(timesTmp.split("#")[0]));
				}else{
					timesList.add(formatTime.parse(timesTmp));
					timesAndIdMap.put("no"+i,timesTmp);
				}
			}catch (ParseException e){
				e.printStackTrace();
			}
		}
		//正序排列
		Collections.sort(timesList);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date begin = null;
		Date end = null;
		Date beginTime = null;
		Date endTime = null;
		try{
			begin = format.parse(nikeJobInfo.getBegin());
			end = format.parse(nikeJobInfo.getEnd());

			beginTime = formatTime.parse(nikeJobInfo.getBegin().split("\\s+")[1]);
			endTime = formatTime.parse(nikeJobInfo.getEnd().split("\\s+")[1]);
		}catch (ParseException e){
			e.printStackTrace();
		}

		jobInfo.setTriggerStartTime(begin);
		jobInfo.setTriggerEndTime(end);

		if(beginTime.compareTo(timesList.get(0)) > 0){
			return new ReturnT<String>(ReturnT.FAIL_CODE,"最早一次启动时间点必须大于开始时间点5秒");
		}
		if(endTime.compareTo(timesList.get(timesList.size()-1)) < 0){
			return new ReturnT<String>(ReturnT.FAIL_CODE,"最晚一次启动时间点必须小于结束时间点5秒");
		}

		//检查所有入参的times，a-有带ID的进行比较修改;b-入参没有的ID，且表中有的ID，进行表数据删除；c-没有带ID的times直接新增
		// 1.删除表中已经存在的jobID 不在修改后的入参中
		xxlJobInfoDao.deleteByIdsAndIdGroup(timesIdList,idGroup);
		// 2.修改表中与入参同时都存在jobID的信息，新增入参中没有带jobID的Job入表
		String returnTimes = "";
		List<String> returnTimesList = new ArrayList<>();
		Set keySet = (Set)timesAndIdMap.keySet();
		Iterator it= keySet.iterator();
		while(it.hasNext()){
			String key= (String) it.next();
			String value=timesAndIdMap.get(key);
			if(key.contains("no")){
				// insert
				if(nikeJobInfo.getFrequency().equals("everyday")){
					String[] timesStr = value.split(":");
					jobInfo.setScheduleConf(Integer.parseInt(timesStr[2])+" "+Integer.parseInt(timesStr[1])+" "+Integer.parseInt(timesStr[0])+" "+"* * ?");
					xxlJobInfoDao.save(jobInfo);
					String id = String.valueOf(jobInfo.getId());
					returnTimesList.add(id+"#"+value);
				}
			}else{
				//update
				if(nikeJobInfo.getFrequency().equals("everyday")){
					String[] timesStr = value.split(":");
					jobInfo.setId(Integer.parseInt(key));
					jobInfo.setScheduleConf(Integer.parseInt(timesStr[2])+" "+Integer.parseInt(timesStr[1])+" "+Integer.parseInt(timesStr[0])+" "+"* * ?");
					xxlJobInfoDao.update(jobInfo);
					String id = String.valueOf(jobInfo.getId());
					returnTimesList.add(id+"#"+value);
				}
			}
		}
		if(returnTimesList.size()>1){
			returnTimes = returnTimesList.get(0);
			for(int k=1; k<returnTimesList.size();k++){
				returnTimes = returnTimes+"-"+returnTimesList.get(k);
			}
		}else {
			returnTimes = returnTimesList.get(0);
		}
		nikeJobInfo.setTimes(returnTimes);
		JSONObject jsonObject = (JSONObject) JSONObject.toJSON(nikeJobInfo);
		nikeJobListEntity.setData(jsonObject.toString());
		JSONObject jsonObjectReturn = (JSONObject) JSONObject.toJSON(nikeJobListEntity);
		return new ReturnT<String>(jsonObjectReturn.toString());
	}
*/

	@Override
	public ReturnT<String> queryJobExeRecs(NikeJobInfoRequest nikeJobInfoRequest) {
//		List<Integer> ids = xxlJobInfoDao.getIdByIdGroup(nikeJobInfoRequest.getId());
//		List<XxlJobLog> logList = xxlJobLogDao.loadList(ids);
		List<XxlJobLog> logList = xxlJobLogDao.loadByJobIdGroup(nikeJobInfoRequest.getJourneyId());
		Gson gson = new Gson();
		String listToJsonString = gson.toJson(logList);
		return new ReturnT<String>(listToJsonString);
	}

	@Override
	public ReturnT<String> queryJobExeRecsPageList(NikeJobLogRequest nikeJobLogRequest) {
		int start = nikeJobLogRequest.getStart();
		int length = nikeJobLogRequest.getLength();
		int jobGroup = nikeJobLogRequest.getJobGroup();
		int jobIdGroup = nikeJobLogRequest.getJourneyId();
		String filterTime = nikeJobLogRequest.getFilterTime();
		int logStatus = nikeJobLogRequest.getLogStatus();

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
		List<XxlJobLog> list = xxlJobLogDao.recsPageList(start, length, jobGroup, jobIdGroup, triggerTimeStart, triggerTimeEnd, logStatus);
		int list_count = xxlJobLogDao.recsPageListCount(start, length, jobGroup, jobIdGroup, triggerTimeStart, triggerTimeEnd, logStatus);

		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("recordsTotal", list_count);		// 总记录数
		maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
		maps.put("data", list);  					// 分页列表

		Gson gson = new Gson();
		String mapsToJsonString = gson.toJson(maps);
		return new ReturnT<String>(mapsToJsonString);
	}

	@Override
	public ReturnT<String> autoStartJobs(){
		xxlJobInfoDao.autoStartJobs();
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> autoStopJobs(){
		xxlJobInfoDao.autoStopJobs();
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> manualStartJobs(NikeJobInfoRequest nikeJobInfoRequest){
		xxlJobInfoDao.manualStartJobs(nikeJobInfoRequest.getJourneyId());
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> manualStopJobs(NikeJobInfoRequest nikeJobInfoRequest){
		xxlJobInfoDao.manualStopJobs(nikeJobInfoRequest.getJourneyId());
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> queryJobNextStart(NikeJobInfoRequest nikeJobInfoRequest){
		NikeJobInfoResponse response = new NikeJobInfoResponse();
		Integer idGroup = nikeJobInfoRequest.getJourneyId();
		response.setJourneyId(idGroup);
		List<XxlJobInfo> jobInfoList = xxlJobInfoDao.getJobsByIdGroup(idGroup);
		Date begin = jobInfoList.get(0).getTriggerStartTime();
		Date end = jobInfoList.get(0).getTriggerEndTime();
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
		Gson gson = new Gson();
		String jsonString = gson.toJson(response);
		return new ReturnT<String>(jsonString);
	}

}
