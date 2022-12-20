package com.nike.ncp.scheduler.core.model;

import lombok.Data;

import java.util.List;

@Data
public class JourneyLogRes {

    private JourneyLogPage page;

    private List<XxlJobLog> data;

}
