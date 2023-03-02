package com.nike.ncp.scheduler.core.util;


import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class CronUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronUtil.class);

    private CronUtil() {

    }

    @SuppressWarnings("all")
    public static String cronNextStart(String cronStr, Date startDate) {
        String dateString = null;
        try {
            //导包import org.quartz.CronExpression
            CronExpression cronExpression = new CronExpression(cronStr);
            Date date = cronExpression.getTimeAfter(startDate);
            //将date转换为指定日期格式字符串
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = dataFormat.format(date);
            //dateString为转换后的日期格式
        } catch (Exception e) {
            LOGGER.error("cron get next execute time error " + e);
        }
        return dateString;
    }

    public static String cronListNextStart(List<String> cronStrList, Date startDate) {
        List<Date> nextStartList = new ArrayList<>();
        for (int i = 0; i < cronStrList.size(); i++) {
            nextStartList.add(UtcLocalDateUtil.strToDate(cronNextStart(cronStrList.get(i), startDate)));
        }
        Collections.sort(nextStartList);
        return UtcLocalDateUtil.localStrToUtcStr(UtcLocalDateUtil.dateToStr(nextStartList.get(0)));
    }

    public static void main(String[] args) {
        System.out.println(cronNextStart("0 50 18 * * ?", new Date()));
        System.out.println(UtcLocalDateUtil.localStrToUtcStr(cronNextStart("0 50 18 * * ?", new Date())));
        List<String> cronStrList = new ArrayList<>();
        cronStrList.add("0 50 17 * * ?");
        cronStrList.add("0 50 19 * * ?");
        cronStrList.add("0 50 18 * * ?");
        cronStrList.add("0 50 16 * * ?");
        System.out.println(cronListNextStart(cronStrList, UtcLocalDateUtil.strToDate("2022-11-25 16:55:00")));
    }
}
