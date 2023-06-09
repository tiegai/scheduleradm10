package com.nike.ncp.scheduler.controller;

import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.enums.ExecutorBlockStrategyEnum;
import com.nike.ncp.scheduler.common.glue.GlueTypeEnum;
import com.nike.ncp.scheduler.common.util.DateUtil;
import com.nike.ncp.scheduler.core.model.XxlJobGroup;
import com.nike.ncp.scheduler.core.model.XxlJobInfo;
import com.nike.ncp.scheduler.core.model.XxlJobUser;
import com.nike.ncp.scheduler.core.route.ExecutorRouteStrategyEnum;
import com.nike.ncp.scheduler.core.thread.JobTriggerPoolHelper;
import com.nike.ncp.scheduler.core.trigger.TriggerTypeEnum;
import com.nike.ncp.scheduler.dao.XxlJobGroupDao;
import com.nike.ncp.scheduler.core.exception.XxlJobException;
import com.nike.ncp.scheduler.service.XxlJobService;
import com.nike.ncp.scheduler.core.scheduler.MisfireStrategyEnum;
import com.nike.ncp.scheduler.core.scheduler.ScheduleTypeEnum;
import com.nike.ncp.scheduler.core.thread.JobScheduleHelper;
import com.nike.ncp.scheduler.core.util.I18nUtil;
import com.nike.ncp.scheduler.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * index controller
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobInfoController.class);

    @Resource
    private transient XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private transient XxlJobService xxlJobService;

    @RequestMapping
    public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

        // 枚举-字典
        model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());        // 路由策略-列表
        model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());                                // Glue类型-字典
        model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());        // 阻塞处理策略-字典
        model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());                        // 调度类型
        model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());                    // 调度过期策略

        // 执行器列表
        List<XxlJobGroup> jobGroupListAll = xxlJobGroupDao.findAll();

        // filter group
        List<XxlJobGroup> jobGroupList = filterJobGroupByRole(request, jobGroupListAll);
        if (jobGroupList == null || jobGroupList.size() == 0) {
            throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
        }

        model.addAttribute("JobGroupList", jobGroupList);
        model.addAttribute("jobGroup", jobGroup);

        return "jobinfo/jobinfo.index";
    }

    @SuppressWarnings("all")
    public static List<XxlJobGroup> filterJobGroupByRole(HttpServletRequest request, List<XxlJobGroup> jobGroupListAll) {
        List<XxlJobGroup> jobGroupList = new ArrayList<>();
        if (jobGroupListAll != null && jobGroupListAll.size() > 0) {
            XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
            if (loginUser.getRole() == 1) {
                jobGroupList = jobGroupListAll;
            } else {
                List<String> groupIdStrs = new ArrayList<>();
                if (loginUser.getPermission() != null && loginUser.getPermission().trim().length() > 0) {
                    groupIdStrs = Arrays.asList(loginUser.getPermission().trim().split(","));
                }
                for (XxlJobGroup groupItem : jobGroupListAll) {
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
            throw new RuntimeException(I18nUtil.getString("system_permission_limit") + "[username=" + loginUser.getUsername() + "]");
        }
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start, @RequestParam(required = false, defaultValue = "10") int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {

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
    @SuppressWarnings("all")
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
            LOGGER.error(e.getMessage(), e);
            return new ReturnT<List<String>>(ReturnT.FAIL_CODE, (I18nUtil.getString("schedule_type") + I18nUtil.getString("system_unvalid")) + e.getMessage());
        }
        return new ReturnT<List<String>>(result);

    }


}
