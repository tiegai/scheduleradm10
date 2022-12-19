package com.nike.ncp.core.model;

import lombok.Data;

import java.util.List;

@Data
public class JourneyLog {

    private int journeyId;

    private int page;

    private int size;

    private int status;

    private String filterTime;

    private int recordsTotal;

    private int recordsFiltered;

    private List<XxlJobLog> journeyLogList;


}
