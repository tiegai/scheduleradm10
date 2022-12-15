package com.nike.springboottemplate.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UtcLocalDateUtil {

    public static String utcStrToLocalStr(String dateStr){
        SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String returnDate = null;
        try{
            Date date = sdfUtc.parse(dateStr);
            returnDate = sdf.format(date);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    public static String localStrToUtcStr(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        String returnDate = null;
        try{
            Date date = sdf.parse(dateStr);
            returnDate = sdfUtc.format(date);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }

    public static Date strToDate(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try{
            date = sdf.parse(dateStr);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String dateToStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        return dateStr;
    }

    public static Date strToTime(String dateStr){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        try{
            date = sdf.parse(dateStr);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static String utcToStr(String dateStr){
        String returnDate = null;
        try {
            SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdfUtc.parse(dateStr);
            returnDate = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnDate;
    }

/*    public static String utcTimeStrToLocalTimeStr(String dateStr){
        SimpleDateFormat sdfUtc = new SimpleDateFormat("HH:mm:ss'Z'");
        sdfUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String returnDate = null;
        try{
            Date date = sdfUtc.parse(dateStr);
            returnDate = sdf.format(date);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return returnDate;
    }*/

    public static void main(String[] args) {
        //System.out.println(utcStrToLocalStr("2022-12-01T23:00Z"));
        //System.out.println(strToDate(utcToLocalStr("2022-12-01T23:00Z")));
        //System.out.println(strToTime("10:12:00"));
        //System.out.println(utcTimeToLocalStr("10:12Z"));
        //System.out.println(utcToStr("2022-12-01T23:00Z"));
        //System.out.println(localStrToUtcStr("2022-11-24 18:50:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()));
        System.out.println(utcToStr("2022-12-01T23:00:00Z"));
    }
}
