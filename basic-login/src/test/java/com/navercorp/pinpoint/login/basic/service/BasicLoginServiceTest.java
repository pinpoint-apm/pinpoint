/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.login.basic.service;

import com.navercorp.pinpoint.login.basic.config.BasicLoginConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BasicLoginServiceTest {

    @Test
    void createNewCookieShouldUseSecureDefaults() {
        try (AnnotationConfigApplicationContext context = newContext(Map.of(
                "web.security.auth.user", "user:password"
        ))) {
            BasicLoginService service = context.getBean(BasicLoginService.class);

            Cookie cookie = service.createNewCookie("user");

            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
            assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
        }
    }

    @Test
    void createNewCookieShouldUseConfiguredSecurityFlags() {
        try (AnnotationConfigApplicationContext context = newContext(Map.of(
                "web.security.auth.user", "user:password",
                "web.security.auth.jwt.cookie.http-only", "false",
                "web.security.auth.jwt.cookie.secure", "false",
                "web.security.auth.jwt.cookie.same-site", "Strict"
        ))) {
            BasicLoginService service = context.getBean(BasicLoginService.class);

            Cookie cookie = service.createNewCookie("user");

            assertThat(cookie.isHttpOnly()).isFalse();
            assertThat(cookie.getSecure()).isFalse();
            assertThat(cookie.getAttribute("SameSite")).isEqualTo("Strict");
        }
    }

    @Test
    void userDetailsServiceShouldReturnCredentialCopy() {
        try (AnnotationConfigApplicationContext context = newContext(Map.of(
                "web.security.auth.user", "user:password"
        ))) {
            BasicLoginService service = context.getBean(BasicLoginService.class);

            UserDetails userDetails = service.getUserDetailsService().loadUserByUsername("user");
            ((CredentialsContainer) userDetails).eraseCredentials();

            UserDetails reloaded = service.getUserDetailsService().loadUserByUsername("user");
            assertThat(reloaded.getPassword()).isNotBlank();
        }
    }

    @Test
    void userDetailsServiceShouldThrowWhenUserIsUnknown() {
        try (AnnotationConfigApplicationContext context = newContext(Map.of(
                "web.security.auth.user", "user:password"
        ))) {
            BasicLoginService service = context.getBean(BasicLoginService.class);

            assertThatThrownBy(() -> service.getUserDetailsService().loadUserByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("User not found: unknown");
        }
    }

    @Test
    void getUserDetailsShouldIgnoreJwtForUnknownUser() {
        Cookie cookie;
        try (AnnotationConfigApplicationContext context = newContext(Map.of(
                "web.security.auth.user", "user:password"
        ))) {
            BasicLoginService service = context.getBean(BasicLoginService.class);
            cookie = service.createNewCookie("user");
        }

        try (AnnotationConfigApplicationContext context = newContext(Map.of())) {
            BasicLoginService service = context.getBean(BasicLoginService.class);

            assertThat(service.getUserDetails(new Cookie[]{cookie})).isNull();
        }
    }

    private AnnotationConfigApplicationContext newContext(Map<String, Object> properties) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
        context.register(BasicLoginConfiguration.class);
        context.refresh();
        return context;
    }
}
