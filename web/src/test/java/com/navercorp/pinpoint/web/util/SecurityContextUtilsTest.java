package com.navercorp.pinpoint.web.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SecurityContextUtilsTest {
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getPrincipal() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication mock = mock(Authentication.class);
        when(mock.getPrincipal()).thenReturn("UserA");
        context.setAuthentication(mock);

        String principal = SecurityContextUtils.getStringPrincipal();
        Assertions.assertEquals("UserA", principal);
    }

    @Test
    public void getPrincipal_Authentication_null() {
        Assertions.assertNull(SecurityContextUtils.getStringPrincipal());
    }

    @Test
    public void getPrincipal_Principal_null() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication mock = mock(Authentication.class);
        context.setAuthentication(mock);

        Assertions.assertNull(SecurityContextUtils.getStringPrincipal());
    }

    @Test
    public void defaultPrincipal() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication mock = mock(Authentication.class);
        when(mock.getPrincipal()).thenReturn("UserA");
        context.setAuthentication(mock);

        String principal = SecurityContextUtils.defaultStringPrincipal("EMPTY");

        Assertions.assertEquals("UserA", principal);
    }

    @Test
    public void defaultPrincipal_securityContextIsNull() {

        String principal = SecurityContextUtils.defaultStringPrincipal("EMPTY");

        Assertions.assertEquals("EMPTY", principal);
    }

    @Test
    public void defaultPrincipal_principalIsNull() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication mock = mock(Authentication.class);
        context.setAuthentication(mock);

        String principal = SecurityContextUtils.defaultStringPrincipal("EMPTY");

        Assertions.assertEquals("EMPTY", principal);
    }

    @Test
    public void defaultPrincipal_invalid_class_type() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication mock = mock(Authentication.class);
        when(mock.getPrincipal()).thenReturn(Collections.emptyList());
        context.setAuthentication(mock);

        String principal = SecurityContextUtils.defaultStringPrincipal("EMPTY");

        Assertions.assertEquals("EMPTY", principal);
    }

}