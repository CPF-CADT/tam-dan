package com.tamdan.exception.web;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_BAD_REQUEST = "error.badRequest";
    public static final String ERR_CONFLICT = "error.conflict";
    public static final String ERR_FORBIDDEN = "error.forbidden";
    public static final String ERR_NOT_FOUND = "error.notFound";
    public static final String ERR_RATE_LIMIT = "error.rateLimitExceeded";
    public static final String CODE_INVALID_INPUT = "INVALID_INPUT";
    public static final String CODE_INVALID_PASSWORD = "INVALID_PASSWORD";
    public static final String CODE_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String CODE_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String CODE_FORBIDDEN_ACCESS = "FORBIDDEN_ACCESS";
    public static final String CODE_DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    public static final String CODE_EXPIRED_TOKEN = "EXPIRED_TOKEN";
    public static final String CODE_REVOKED_TOKEN = "REVOKED_TOKEN";
    public static final String CODE_EXPIRED_PAIRING_CODE = "EXPIRED_PAIRING_CODE";
    public static final String CODE_REPLAYED_PAIRING_CODE = "REPLAYED_PAIRING_CODE";
    public static final String CODE_INACTIVE_DEVICE = "INACTIVE_DEVICE";
    public static final String CODE_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String CODE_DATABASE_CONFLICT = "DATABASE_CONFLICT";
    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    private ErrorConstants() {}
}
