package com.nike.ncp.scheduler.controller;

import com.nike.ncp.scheduler.core.model.JourneyInfo;
import com.nike.ncp.scheduler.core.model.JourneyNextStart;
import com.nike.ncp.scheduler.dao.XxlJobInfoDao;
import java.util.ArrayList;
import java.util.List;

import com.nike.ncp.scheduler.service.impl.SchedulerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceImplTest {
    private SchedulerServiceImpl schedulerService;

    @Mock
    private XxlJobInfoDao xxlJobInfoDao;


    @Test
    void addJobs() {
        schedulerService = spy(new SchedulerServiceImpl());
        schedulerService.setJobGroup(1);
        schedulerService.setXxlJobInfoDao(xxlJobInfoDao);

        List<String> cronList = new ArrayList<>();
        cronList.add("0 50 17 * * ?");

        JourneyInfo journeyInfo = new JourneyInfo();

        journeyInfo.setJourneyId("7064");
        journeyInfo.setDescription("执行一次");
        journeyInfo.setCreatedTime("2022-12-18T12:49:00Z");
        journeyInfo.setModifiedTime("2023-1-18T12:49:00Z");
        journeyInfo.setPeriodicType("ONCE");
        journeyInfo.setNextStartTime("2023-02-18T14:00:00Z");


        Mockito.when(xxlJobInfoDao.save(any())).thenReturn(1);

        when(xxlJobInfoDao.getCronByJourneyId(anyString())).thenReturn(cronList);

        JourneyNextStart response = schedulerService.addJobs(journeyInfo, "jch527", "jerry");

        verify(xxlJobInfoDao).save(any());

        assertNotNull(response);

    }

    @Test
    void modifyJob() {
    }

    @Test
    void autoStartJobs() {
    }

    @Test
    void autoStopJobs() {
    }

    @Test
    void manualStartJobs() {
    }

    @Test
    void manualStopJobs() {
    }

    @Test
    void deleteJobs() {
    }

    @Test
    void queryJobExeRecs() {
    }

    @Test
    void queryJobNextStart() {
    }
}