package com.bansikah.secureportal.user.domain;

import java.util.Set;

public enum Role {
    USER(Set.of(Permission.PROFILE_READ, Permission.PROFILE_WRITE, Permission.REPORT_READ)),
    MANAGER(Set.of(Permission.PROFILE_READ, Permission.PROFILE_WRITE, Permission.REPORT_READ,
            Permission.REPORT_GENERATE, Permission.USER_READ)),
    ADMIN(Set.of(Permission.PROFILE_READ, Permission.PROFILE_WRITE, Permission.REPORT_READ,
            Permission.REPORT_GENERATE, Permission.USER_READ, Permission.USER_WRITE,
            Permission.USER_DELETE, Permission.ADMIN_READ, Permission.ADMIN_WRITE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}