package com.classpets.backend.auth.security;

/**
 * A simple principal object to hold teacher information in the security
 * context.
 */
public class TeacherPrincipal {

    private final Long teacherId;
    private final String username;

    public TeacherPrincipal(Long teacherId, String username) {
        this.teacherId = teacherId;
        this.username = username;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "TeacherPrincipal{teacherId=" + teacherId + ", username='" + username + "'}";
    }
}
