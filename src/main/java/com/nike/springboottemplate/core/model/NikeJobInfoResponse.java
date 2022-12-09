package com.nike.springboottemplate.core.model;

public class NikeJobInfoResponse {

    private int journeyId;

    private String nextStartTime;

    public int getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(int journeyId) {
        this.journeyId = journeyId;
    }

    public String getNextStartTime() {
        return nextStartTime;
    }

    public void setNextStartTime(String nextStartTime) {
        this.nextStartTime = nextStartTime;
    }
}
