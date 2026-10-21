package com.tamdan.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import tech.jhipster.config.JHipsterConstants;

/**
 * Aspect for logging execution of service and repository Spring components.
 *
 * By default, it only runs with the "dev" profile.
 */
@Aspect
@RequiredArgsConstructor
public class LoggingAspect {

    private final Environment env;

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut(
        """
        within(@org.springframework.stereotype.Repository *)
        || within(@org.springframework.stereotype.Service *)
        || within(@org.springframework.web.bind.annotation.RestController *)
        """
    )
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advice methods.
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut(
        """
        within(com.tamdan.repository..*)
        || within(com.tamdan.service..*)
        || within(com.tamdan.controller..*)
        """
    )
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advice methods.
    }

    /**
     * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
     *
     * @param joinPoint join point we want the logger for.
     * @return {@link Logger} associated to the given {@link JoinPoint}.
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice.
     * @param e exception.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        logException(joinPoint, e);
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice.
     * @return result.
     * @throws Throwable rethrows the exception thrown by the advised method.
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        var log = logger(joinPoint);
        if (log.isDebugEnabled()) {
            log.debug("Enter: {}()", joinPoint.getSignature().getName());
        }
        try {
            Object result = joinPoint.proceed();
            logExit(log, joinPoint);
            return result;
        } catch (IllegalArgumentException e) {
            logIllegalArgument(log, joinPoint);
            throw e;
        }
    }

    private void logExit(Logger log, JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            log.debug("Exit: {}()", joinPoint.getSignature().getName());
        }
    }

    private void logIllegalArgument(Logger log, JoinPoint joinPoint) {
        if (log.isErrorEnabled()) {
            log.error("Illegal argument in {}()", joinPoint.getSignature().getName());
        }
    }

    private void logException(JoinPoint joinPoint, Throwable e) {
        boolean sensitive = containsSensitiveValue(e);
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT))) {
            logger(joinPoint).error(
                "Exception in {}() with cause = '{}' and exception = '{}'",
                joinPoint.getSignature().getName(),
                sensitive ? "[REDACTED]" : (e.getCause() != null ? e.getCause() : "NULL"),
                sensitive ? "[REDACTED]" : e.getMessage(),
                sensitive ? null : e
            );
        } else {
            logger(joinPoint).error(
                "Exception in {}() with cause = {}",
                joinPoint.getSignature().getName(),
                sensitive ? "[REDACTED]" : (e.getCause() != null ? String.valueOf(e.getCause()) : "NULL")
            );
        }
    }

    private boolean containsSensitiveValue(Throwable exception) {
        String message = exception.getMessage();
        if (message == null) return false;
        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("password") || normalized.contains("token") || normalized.contains("credential") || normalized.contains("pairing");
    }
}
