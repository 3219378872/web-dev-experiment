package com.hmall.common.utils;

public class UserContext {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();
    private static final ThreadLocal<String> roleTL = new ThreadLocal<>();

    public static void setUser(Long userId) {
        tl.set(userId);
    }

    public static Long getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }

    public static void setRole(String role) {
        roleTL.set(role);
    }

    public static String getRole() {
        return roleTL.get();
    }

    public static void removeRole() {
        roleTL.remove();
    }

    public static void clear() {
        removeUser();
        removeRole();
    }
}
