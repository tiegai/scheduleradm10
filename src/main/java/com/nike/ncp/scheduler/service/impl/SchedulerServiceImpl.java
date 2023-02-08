package com.nike.ncp.scheduler.service.impl;

import com.nike.ncp.scheduler.common.util.DateUtil;
import com.nike.ncp.scheduler.core.constant.ConCollections;
import com.nike.ncp.scheduler.core.util.CronUtil;
import com.nike.ncp.scheduler.core.util.UtcLocalDateUtil;
import com.nike.ncp.scheduler.dao.XxlJobInfoDao;
import com.nike.ncp.scheduler.dao.XxlJobLogDao;
import com.nike.ncp.scheduler.dao.XxlJobLogGlueDao;
import com.nike.ncp.scheduler.core.model.JourneyInfo;
import com.nike.ncp.scheduler.core.model.JourneyLogRes;
import com.nike.ncp.scheduler.core.model.JourneyNextStart;
import com.nike.ncp.scheduler.core.model.XxlJobInfo;
import com.nike.ncp.scheduler.core.model.XxlJobLog;
import com.nike.ncp.scheduler.core.model.JourneyLogPage;
import com.nike.ncp.scheduler.exception.ApiExceptions;
import com.nike.ncp.scheduler.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Value("${ncp.engine.url.start.begin}")
    private transient String engineUrlStartBegin;

    @Value("${ncp.engine.url.start.end}")
    private transient String engineUrlStartEnd;

    @Value("${ncp.engine.url}")
    private transient String engineUrl;

    @Value("${ncp.engine.suspend}")
    private transient String suspend;

    @Value("${ncp.engine.resume}")
    private transient String resume;

    @Value("${ncp.scheduler.executor.job.group}")
    private transient Integer jobGroup;

    @Resource
    private transient XxlJobInfoDao xxlJobInfoDao;

    @Resource
    private transient XxlJobLogDao xxlJobLogDao;

    @Resource
    private transient XxlJobLogGlueDao xxlJobLogGlueDao;

    private static final int SIZE_ONE = 1;

    private static final int TRIGGER_STATUS_RUN = 1;

    private static final int STATUS_CODE_SUCCESS = 200;

    @Override
    @Transactional
    @SuppressWarnings("all")
    public JourneyNextStart addJobs(JourneyInfo journeyInfo, String userId, String userName) {
        // add in db
        XxlJobInfo jobInfo = new XxlJobInfo();
        String journeyId = journeyInfo.getJourneyId();
        LOGGER.info(journeyId + "begin addJobs by " + userId + "and userName is " + userName);
        jobInfo.setJourneyId(journeyId);
        jobInfo.setJobGroup(jobGroup);
        jobInfo.setJobDesc(journeyInfo.getDescription());
        jobInfo.setAddTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getCreatedTime())));
        jobInfo.setUpdateTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getModifiedTime())));
        jobInfo.setGlueUpdatetime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getModifiedTime())));
        String beginStr = journeyInfo.getPeriodicBegin();
        String endStr = journeyInfo.getPeriodicEnd();
        jobInfo.setAuthor(userId);
        jobInfo.setScheduleType(ConCollections.SCHEDULE_TYPE);
        jobInfo.setGlueType(ConCollections.GLUE_TYPE);
        jobInfo.setExecutorRouteStrategy(ConCollections.EXECUTOR_ROUTE_STRATEGY);
        jobInfo.setMisfireStrategy(ConCollections.MISFIRE_STRATEGY);
        jobInfo.setExecutorBlockStrategy(ConCollections.EXECUTOR_BLOCK_STRATEGY);
        jobInfo.setExecutorHandler(ConCollections.EXECUTOR_HANDLER);
        jobInfo.setExecutorParam(engineUrlStartBegin + journeyId + engineUrlStartEnd);
        String[] times = null;
        List<Date> timesList = new ArrayList<>();
        //once
        if (journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)) {
            if (journeyInfo.getNextStartTime() == null) {
                throw ApiExceptions.invalidRequest().with(9003, journeyId + "nextStartTime is null");
            }
        } else { // not once
            times = journeyInfo.getPeriodicTimes().split(",");
            if (times.length < 1) {
                throw ApiExceptions.invalidRequest().with(9002, journeyId + "periodicTimes is error");
            }
            for (int i = 0; i < times.length; i++) {
                timesList.add(UtcLocalDateUtil.strToTime(times[i] + ":00"));
            }
        }
        if (journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)) {
            String nextStart = UtcLocalDateUtil.utcStrToLocalStr(journeyInfo.getNextStartTime());
            Integer year = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[0]);
            Integer month = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[1]);
            Integer day = Integer.parseInt(nextStart.split("\\s+")[0].split("-")[2]);
            Integer hour = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[0]);
            Integer minute = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[1]);
            Integer second = Integer.parseInt(nextStart.split("\\s+")[1].split(":")[2]);
            //0 40 17 2 12 ? 2022-2022
            jobInfo.setScheduleConf(second + " " + minute + " " + hour + " " + day + " " + month + " " + "?" + " " + year + "-" + year);
            jobInfo.setTriggerStatus(TRIGGER_STATUS_RUN);
            xxlJobInfoDao.save(jobInfo);
        } else if (journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_DAILY)) {
            jobInfo.setJourneyStartTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr)));
            jobInfo.setJourneyEndTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(endStr)));
            for (int i = 0; i < times.length; i++) {
                String[] timesStr = times[i].split(":");
                jobInfo.setScheduleConf(Integer.parseInt("0") + " " + Integer.parseInt(timesStr[1]) + " " + Integer.parseInt(timesStr[0]) + " " + "* * ?");
                xxlJobInfoDao.save(jobInfo);
            }
        } else if (journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_WEEKLY)) {
            jobInfo.setJourneyStartTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr)));
            jobInfo.setJourneyEndTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(endStr)));
            for (int i = 0; i < times.length; i++) {
                String[] timesStr = times[i].split(":");
                String week = journeyInfo.getPeriodicValues();
                jobInfo.setScheduleConf(Integer.parseInt("0") + " " + Integer.parseInt(timesStr[1]) + " " + Integer.parseInt(timesStr[0]) + " " + "? *" + " " + week);
                xxlJobInfoDao.save(jobInfo);
            }
        } else if (journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_MONTHLY)) {
            jobInfo.setJourneyStartTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr)));
            jobInfo.setJourneyEndTime(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(endStr)));
            for (int i = 0; i < times.length; i++) {
                String[] timesStr = times[i].split(":");
                String day = journeyInfo.getPeriodicValues();
                jobInfo.setScheduleConf(Integer.parseInt("0") + " " + Integer.parseInt(timesStr[1]) + " " + Integer.parseInt(timesStr[0]) + " " + day + " " + "* ?");
                xxlJobInfoDao.save(jobInfo);
            }
        }

        JourneyNextStart response = new JourneyNextStart();
        response.setJourneyId(journeyId);
        // 校验是否全成功创建所有job,并返回nextStart
        List<String> cronList = xxlJobInfoDao.getCronByJourneyId(journeyId);
        if (journeyInfo.getPeriodicType().equals(ConCollections.PERIODIC_ONCE)) {
            if (cronList.size() != 1) {
                throw ApiExceptions.invalidRequest().with(9001, journeyId + "journeyId is duplicate insertion");
            }
            response.setNextStartTime(journeyInfo.getNextStartTime());
        } else {
            if (cronList.size() != timesList.size()) {
                throw ApiExceptions.invalidRequest().with(9001, journeyId + "journeyId is duplicate insertion");
            }
            response.setNextStartTime(CronUtil.cronListNextStart(cronList, UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(beginStr))));
        }
        return response;
    }


    @Override
    @Transactional
    public JourneyNextStart modifyJob(String journeyId, JourneyInfo journeyInfo, String userId, String userName) {
        LOGGER.info(journeyId + "begin modify by " + userId + "and userName is " + userName);
        xxlJobInfoDao.deleteByJourneyId(journeyId);
        journeyInfo.setJourneyId(journeyId);
        JourneyNextStart journeyNextStart = addJobs(journeyInfo, userId, userName);
        return journeyNextStart;
    }


    @Override
    public void autoStartJobs() {
        xxlJobInfoDao.autoStartJobs();
    }

    @Override
    public void autoStopJobs() {
        xxlJobInfoDao.autoStopJobs();
    }

    @Override
    @Transactional
    public void manualStartJobs(String journeyId, String userId, String userName) {
        LOGGER.info(journeyId + "begin manual start by " + userId + "and userName is " + userName);
        xxlJobInfoDao.manualStartJobs(journeyId, userId);
        //String url = engineUrl + journeyId + "/" + userId + "/" + userName + resume;
        String url = engineUrl + journeyId + resume;
        httpHandlerForSuspend(url, userId, userName);
    }

    @Override
    @Transactional
    public void manualStopJobs(String journeyId, String userId, String userName) {
        LOGGER.info(journeyId + "begin manual stop by " + userId + "and userName is " + userName);
        xxlJobInfoDao.manualStopJobs(journeyId, userId);
        String url = engineUrl + journeyId + suspend;
        httpHandlerForSuspend(url, userId, userName);
    }

    @Override
    @Transactional
    public void deleteJobs(String journeyId, String userId, String userName) {
        LOGGER.info(journeyId + "begin delete by " + userId + "and userName is " + userName);
        List<String> cronList = xxlJobInfoDao.getCronByJourneyId(journeyId);
        if (cronList.size() < SIZE_ONE) {
            return;
        }
        xxlJobInfoDao.deleteByJourneyId(journeyId);
        xxlJobLogDao.deleteByJourneyId(journeyId);
        xxlJobLogGlueDao.deleteByJourneyId(journeyId);
    }

    @Override
    @SuppressWarnings("all")
    public JourneyLogRes queryJobExeRecs(String journeyId, int page, int size, int status, String filterTime) {

        Date triggerTimeStart = null;
        Date triggerTimeEnd = null;
        if (filterTime != null && filterTime.trim().length() > 0) {
            String[] temp = filterTime.split(" - ");
            if (temp.length == 2) {
                triggerTimeStart = DateUtil.parseDateTime(temp[0]);
                triggerTimeEnd = DateUtil.parseDateTime(temp[1]);
            }
        }
        // page query
        List<XxlJobLog> list = xxlJobLogDao.recsPageList(page, size, journeyId, triggerTimeStart, triggerTimeEnd, status);
        int listCount = xxlJobLogDao.recsPageListCount(page, size, journeyId, triggerTimeStart, triggerTimeEnd, status);

        JourneyLogRes journeyLogRes = new JourneyLogRes();
        JourneyLogPage journeyLogPage = new JourneyLogPage();
        journeyLogPage.setTotal(listCount);
        journeyLogPage.setSize(size);
        journeyLogPage.setCurrent(page);
        journeyLogPage.setPages(page);
        journeyLogRes.setData(list);
        journeyLogRes.setPage(journeyLogPage);
        return journeyLogRes;
    }

    @Override
    public JourneyNextStart queryJobNextStart(String journeyId, String userId, String userName) {
        LOGGER.info(journeyId + "begin queryJobNextStart by " + userId + "and userName is " + userName);
        JourneyNextStart response = new JourneyNextStart();
        response.setJourneyId(journeyId);
        List<XxlJobInfo> jobInfoList = xxlJobInfoDao.getJobsByJourneyId(journeyId);
        if (jobInfoList == null || jobInfoList.size() < 1) {
            throw ApiExceptions.internalError();
        }
        Date begin = jobInfoList.get(0).getJourneyStartTime();
        Date end = jobInfoList.get(0).getJourneyEndTime();
        List<String> cronList = new ArrayList<>();
        Date start = new Date();
        if (start.before(begin)) {
            start = begin;
        }
        for (int i = 0; i < jobInfoList.size(); i++) {
            cronList.add(jobInfoList.get(i).getScheduleConf());
        }
        String nextStart = CronUtil.cronListNextStart(cronList, start);
        if (end.before(UtcLocalDateUtil.strToDate(UtcLocalDateUtil.utcStrToLocalStr(nextStart)))) {
            response.setNextStartTime("no next start");
        } else {
            response.setNextStartTime(nextStart);
        }
        return response;
    }

    @SuppressWarnings("all")
    public void httpHandlerForSuspend(String url, String userId, String userName) {

        System.out.println("EngineUrl:" + url);

        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try {
            // connection
            URL realUrl = new URL(url);
            System.out.println("EngineRealUrl:" + realUrl);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(5 * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");
            connection.setRequestProperty("userId", userId);
            connection.setRequestProperty("userName", userName);

            // do connection
            connection.connect();

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != STATUS_CODE_SUCCESS) {
                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String responseMsg = result.toString();

            LOGGER.info(responseMsg);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
                LOGGER.info("access engine success");
            } catch (Exception e2) {
                LOGGER.error(e2.getMessage(), e2);
            }
        }

    }

}
