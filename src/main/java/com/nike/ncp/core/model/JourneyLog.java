package com.nike.ncp.core.model;

import java.util.List;

public class JourneyLog {

    private int journeyId;

    private int page;

    private int size;

    private int status;

    private String filterTime;

    private int recordsTotal;

    private int recordsFiltered;

    private List<XxlJobLog> journeyLogList;

    public int getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(int journeyId) {
        this.journeyId = journeyId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFilterTime() {
        return filterTime;
    }

    public void setFilterTime(String filterTime) {
        this.filterTime = filterTime;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public List<XxlJobLog> getJourneyLogList() {
        return journeyLogList;
    }

    public void setJourneyLogList(List<XxlJobLog> journeyLogList) {
        this.journeyLogList = journeyLogList;
    }
}
