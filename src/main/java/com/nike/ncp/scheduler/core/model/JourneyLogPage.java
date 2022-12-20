package com.nike.ncp.scheduler.core.model;

import lombok.Data;

@Data
public class JourneyLogPage {

    private int total;

    private int size;

    private int current;

    private int pages;
}
