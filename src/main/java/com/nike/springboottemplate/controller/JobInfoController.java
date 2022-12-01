package com.nike.springboottemplate.controller;

import com.nike.springboottemplate.controller.annotation.PermissionLimit;
import com.nike.springboottemplate.core.exception.XxlJobException;
import com.nike.springboottemplate.core.model.*;
import com.nike.springboottemplate.core.route.ExecutorRouteStrategyEnum;
import com.nike.springboottemplate.core.scheduler.MisfireStrategyEnum;
import com.nike.springboottemplate.core.scheduler.ScheduleTypeEnum;
import com.nike.springboottemplate.core.thread.JobScheduleHelper;
import com.nike.springboottemplate.core.thread.JobTriggerPoolHelper;
import com.nike.springboottemplate.core.trigger.TriggerTypeEnum;
import com.nike.springboottemplate.core.util.I18nUtil;
import com.nike.springboottemplate.dao.XxlJobGroupDao;
import com.nike.springboottemplate.service.LoginService;
import com.nike.springboottemplate.service.XxlJobService;
import com.nike.springboottemplate.common.biz.model.ReturnT;
import com.nike.springboottemplate.common.enums.ExecutorBlockStrategyEnum;
import com.nike.springboottemplate.common.glue.GlueTypeEnum;
import com.nike.springboottemplate.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * index controller
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {
	private static Logger logger = LoggerFactory.getLogger(JobInfoController.class);

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;
	@Resource
	private XxlJobService xxlJobService;
	
	@RequestMapping
	public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
		model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
		model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

		// 执行器列表
		List<XxlJobGroup> jobGroupList_all =  xxlJobGroupDao.findAll();

		// filter group
		List<XxlJobGroup> jobGroupList = filterJobGroupByRole(request, jobGroupList_all);
		if (jobGroupList==null || jobGroupList.size()==0) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}

		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);

		return "jobinfo/jobinfo.index";
	}

	public static List<XxlJobGroup> filterJobGroupByRole(HttpServletRequest request, List<XxlJobGroup> jobGroupList_all){
		List<XxlJobGroup> jobGroupList = new ArrayList<>();
		if (jobGroupList_all!=null && jobGroupList_all.size()>0) {
			XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
			if (loginUser.getRole() == 1) {
				jobGroupList = jobGroupList_all;
			} else {
				List<String> groupIdStrs = new ArrayList<>();
				if (loginUser.getPermission()!=null && loginUser.getPermission().trim().length()>0) {
					groupIdStrs = Arrays.asList(loginUser.getPermission().trim().split(","));
				}
				for (XxlJobGroup groupItem:jobGroupList_all) {
					if (groupIdStrs.contains(String.valueOf(groupItem.getId()))) {
						jobGroupList.add(groupItem);
					}
				}
			}
		}
		return jobGroupList;
	}
	public static void validPermission(HttpServletRequest request, int jobGroup) {
		XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
		if (!loginUser.validPermission(jobGroup)) {
			throw new RuntimeException(I18nUtil.getString("system_permission_limit") + "[username="+ loginUser.getUsername() +"]");
		}
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,  
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {
		
		return xxlJobService.pageList(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
	}
	
	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(XxlJobInfo jobInfo) {
		return xxlJobService.add(jobInfo);
	}
	
	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(XxlJobInfo jobInfo) {
		return xxlJobService.update(jobInfo);
	}
	
	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		return xxlJobService.remove(id);
	}
	
	@RequestMapping("/stop")
	@ResponseBody
	public ReturnT<String> pause(int id) {
		return xxlJobService.stop(id);
	}
	
	@RequestMapping("/start")
	@ResponseBody
	public ReturnT<String> start(int id) {
		return xxlJobService.start(id);
	}
	
	@RequestMapping("/trigger")
	@ResponseBody
	//@PermissionLimit(limit = false)
	public ReturnT<String> triggerJob(int id, String executorParam, String addressList) {
		// force cover job param
		if (executorParam == null) {
			executorParam = "";
		}

		JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, executorParam, addressList);
		return ReturnT.SUCCESS;
	}

	@RequestMapping("/nextTriggerTime")
	@ResponseBody
	public ReturnT<List<String>> nextTriggerTime(String scheduleType, String scheduleConf) {

		XxlJobInfo paramXxlJobInfo = new XxlJobInfo();
		paramXxlJobInfo.setScheduleType(scheduleType);
		paramXxlJobInfo.setScheduleConf(scheduleConf);

		List<String> result = new ArrayList<>();
		try {
			Date lastTime = new Date();
			for (int i = 0; i < 5; i++) {
				lastTime = JobScheduleHelper.generateNextValidTime(paramXxlJobInfo, lastTime);
				if (lastTime != null) {
					result.add(DateUtil.formatDateTime(lastTime));
				} else {
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<List<String>>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid")) + e.getMessage());
		}
		return new ReturnT<List<String>>(result);

	}

	/**
	* @PermissionLimit(limit = false),default is true,login is required authenticate user
	* */
	@RequestMapping("/autoStartJobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> startJobIdsByTriggerBeginTime() {
		System.out.println("per second check,autoStartJobs...");
		return xxlJobService.autoStartJobs();
	}

	@RequestMapping("/autoStopJobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> stopJobIdsByTriggerEndTime() {
		System.out.println("per second check,autoStopJobs...");
		return xxlJobService.autoStopJobs();
	}

	@RequestMapping("/manualStopJobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> manualStopJobs(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.manualStopJobs(nikeJobInfoRequest);
	}

	@RequestMapping("/manualStartJobs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> manualStartJobs(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.manualStartJobs(nikeJobInfoRequest);
	}

	@RequestMapping("/addJobList")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> addJobList(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.addJobList(nikeJobInfoRequest);
	}

	@RequestMapping("/deleteJobList")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> deleteJobList(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.deleteJobList(nikeJobInfoRequest);
	}

	@RequestMapping("/modifyJobList")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> modifyJobList(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.modifyJobList(nikeJobInfoRequest);
	}

	@RequestMapping("/queryJobExeRecs")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> queryJobExeRecs(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.queryJobExeRecs(nikeJobInfoRequest);
	}

	@RequestMapping("/queryJobNextStart")
	@ResponseBody
	@PermissionLimit(limit = false)
	public ReturnT<String> queryJobNextStart(@RequestBody NikeJobInfoRequest nikeJobInfoRequest) {
		return xxlJobService.queryJobNextStart(nikeJobInfoRequest);
	}
	
}
