package com.bansikah.secureportal.user.domain;

public enum Permission {
    PROFILE_READ,
    PROFILE_WRITE,
    REPORT_READ,
    REPORT_GENERATE,
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    ADMIN_READ,
    ADMIN_WRITE;

    public static boolean isDefined(String authority) {
        try {
            valueOf(authority);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}