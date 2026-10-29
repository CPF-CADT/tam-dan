package com.tamdan.web;

public final class ApiPaths {
    public static final String API = "/api";
    public static final String API_V1 = API + "/v1";
    public static final String AUTHENTICATE = API + "/authenticate";
    public static final String AUTHENTICATE_REFRESH = AUTHENTICATE + "/refresh";
    public static final String AUTHENTICATE_LOGOUT = AUTHENTICATE + "/logout";
    public static final String AUTHENTICATE_LOGOUT_ALL = AUTHENTICATE + "/logout-all";
    public static final String ACCOUNT = API + "/account";
    public static final String ADMIN = API + "/admin";
    public static final String AUTHORITIES = API + "/authorities";
    public static final String USERS = API + "/users";
    public static final String V1 = API + "/v1";
    public static final String V1_AUTH = V1 + "/auth";
    public static final String V1_FAMILIES = V1 + "/families/me";
    public static final String V1_PAIRING = V1 + "/pairing";
    public static final String V1_DEVICES = V1 + "/devices";

    private ApiPaths() {}
}
