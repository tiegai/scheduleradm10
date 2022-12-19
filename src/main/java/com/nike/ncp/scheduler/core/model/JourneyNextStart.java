package com.nike.ncp.scheduler.core.model;

import lombok.Data;

@Data
public class JourneyNextStart {

    private int journeyId;

    private String nextStartTime;

}
