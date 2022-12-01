package com.nike.springboottemplate.core.model;

public class NikeJobInfoRequest {

    private int id;

    private int version;

    private int programId;

    private int campaignId;

    private int subCampaignId;

    private String name;

    private String description;

    private int periodicType; //0-once;1-daily;2-weekly;3-monthly

    private String periodicStart;

    private String periodicEnd;

    private String periodicValues;

    private String periodicTimes;

    private String nextStart;

    private int audienceId;

    private String createdTime;

    private String modifiedTime;

    private String journeyAddress;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getSubCampaignId() {
        return subCampaignId;
    }

    public void setSubCampaignId(int subCampaignId) {
        this.subCampaignId = subCampaignId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPeriodicType() {
        return periodicType;
    }

    public void setPeriodicType(int periodicType) {
        this.periodicType = periodicType;
    }

    public String getPeriodicStart() {
        return periodicStart;
    }

    public void setPeriodicStart(String periodicStart) {
        this.periodicStart = periodicStart;
    }

    public String getPeriodicEnd() {
        return periodicEnd;
    }

    public void setPeriodicEnd(String periodicEnd) {
        this.periodicEnd = periodicEnd;
    }

    public String getPeriodicValues() {
        return periodicValues;
    }

    public void setPeriodicValues(String periodicValues) {
        this.periodicValues = periodicValues;
    }

    public String getPeriodicTimes() {
        return periodicTimes;
    }

    public void setPeriodicTimes(String periodicTimes) {
        this.periodicTimes = periodicTimes;
    }

    public int getAudienceId() {
        return audienceId;
    }

    public void setAudienceId(int audienceId) {
        this.audienceId = audienceId;
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

    public String getNextStart() {
        return nextStart;
    }

    public void setNextStart(String nextStart) {
        this.nextStart = nextStart;
    }

    public String getJourneyAddress() {
        return journeyAddress;
    }

    public void setJourneyAddress(String journeyAddress) {
        this.journeyAddress = journeyAddress;
    }
}
