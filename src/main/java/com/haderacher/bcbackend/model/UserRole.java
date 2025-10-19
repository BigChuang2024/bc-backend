package com.haderacher.bcbackend.model;

public enum UserRole {
    STUDENT("ROLE_STUDENT"),
    RECRUITER("ROLE_RECRUITER"),
    ADMIN("ROLE_ADMIN");
    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
