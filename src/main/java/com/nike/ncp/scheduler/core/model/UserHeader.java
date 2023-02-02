package com.nike.ncp.scheduler.core.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public final class UserHeader {

    private UserHeader() {

    }

    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
}
