package com.nike.ncp.scheduler.core.model;

import lombok.Data;

@Data
public class JourneyInfo {

    private int journeyId;

    private String description;

    private String createdTime;

    private String modifiedTime;

    private String periodicBegin;

    private String periodicEnd;

    private String periodicType;

    private String nextStartTime;

    private String periodicTimes;

    private String periodicValues;

//    private int version;

//    private int programId;

//    private int campaignId;

//    private int subCampaignId;

//    private String name;

//    private int audienceId;

//    private String journeyAddress;


}
