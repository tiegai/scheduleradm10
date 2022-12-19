package com.nike.ncp.core.model;

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

    public int getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(int journeyId) {
        this.journeyId = journeyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getPeriodicBegin() {
        return periodicBegin;
    }

    public void setPeriodicBegin(String periodicBegin) {
        this.periodicBegin = periodicBegin;
    }

    public String getPeriodicEnd() {
        return periodicEnd;
    }

    public void setPeriodicEnd(String periodicEnd) {
        this.periodicEnd = periodicEnd;
    }

    public String getPeriodicType() {
        return periodicType;
    }

    public void setPeriodicType(String periodicType) {
        this.periodicType = periodicType;
    }

    public String getNextStartTime() {
        return nextStartTime;
    }

    public void setNextStartTime(String nextStartTime) {
        this.nextStartTime = nextStartTime;
    }

    public String getPeriodicTimes() {
        return periodicTimes;
    }

    public void setPeriodicTimes(String periodicTimes) {
        this.periodicTimes = periodicTimes;
    }

    public String getPeriodicValues() {
        return periodicValues;
    }

    public void setPeriodicValues(String periodicValues) {
        this.periodicValues = periodicValues;
    }


//    private int audienceId;



//    private String journeyAddress;


}
