package com.navercorp.pinpoint.web.util;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SecurityContextUtils {

    private static SecurityContextHolderStrategy STRATEGY = SecurityContextHolder.getContextHolderStrategy();

    static void setStrategy(SecurityContextHolderStrategy strategy) {
        SecurityContextUtils.STRATEGY = Objects.requireNonNull(strategy, "strategy");
    }

    public static String getStringPrincipal() {
        return defaultStringPrincipal(null);
    }

    public static String defaultStringPrincipal(String defaultPrincipal) {
        return defaultPrincipal(String.class, defaultPrincipal);
    }

    public static <T> T getPrincipal(Class<T> clazz) {
        return defaultPrincipal(clazz, null);
    }

    public static <T> T defaultPrincipal(Class<T> clazz, T defaultPrincipal) {
        final Authentication authentication = getAuthentication();
        if (authentication == null) {
            return defaultPrincipal;
        }
        final Object principal = authentication.getPrincipal();
        if (clazz.isInstance(principal)) {
            return cast(principal);
        }
        return defaultPrincipal;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object principal) {
        return (T) principal;
    }

    public static Authentication getAuthentication() {
        final SecurityContext context = getSecurityContext();
        return context.getAuthentication();
    }

    private static String getThreadName() {
        return Thread.currentThread().getName();
    }

    private static SecurityContext getSecurityContext() {
        final SecurityContext context = STRATEGY.getContext();
        if (context == null) {
            throw new AuthenticationCredentialsNotFoundException("SecurityContext not found th:" + getThreadName());
        }
        return context;
    }
}
