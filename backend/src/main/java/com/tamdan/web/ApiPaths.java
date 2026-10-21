package com.tamdan.web;

public final class ApiPaths {
    public static final String API = "/api";
    public static final String AUTHENTICATE = API + "/authenticate";
    public static final String AUTHENTICATE_REFRESH = AUTHENTICATE + "/refresh";
    public static final String AUTHENTICATE_LOGOUT = AUTHENTICATE + "/logout";
    public static final String AUTHENTICATE_LOGOUT_ALL = AUTHENTICATE + "/logout-all";
    public static final String ACCOUNT = API + "/account";
    public static final String ADMIN = API + "/admin";
    public static final String AUTHORITIES = API + "/authorities";
    public static final String USERS = API + "/users";

    private ApiPaths() {}
}
