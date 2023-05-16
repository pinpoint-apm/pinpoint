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

package com.navercorp.pinpoint.login.basic;

import com.navercorp.pinpoint.login.basic.config.BasicLoginConfiguration;
import com.navercorp.pinpoint.login.basic.service.BasicLoginConstants;
import com.navercorp.pinpoint.login.basic.service.BasicLoginService;
import com.navercorp.pinpoint.login.basic.service.JwtRequestFilter;
import com.navercorp.pinpoint.login.basic.service.PreAuthenticationCheckFilter;
import com.navercorp.pinpoint.login.basic.service.SaveJwtTokenAuthenticationSuccessHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

import java.util.Objects;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * @author Taejin Koo
 */
@Configuration
@EnableWebSecurity
@Import(BasicLoginConfiguration.class)
@ConditionalOnProperty(name = "pinpoint.modules.web.login", havingValue = "basicLogin")
public class PinpointBasicLoginConfig {

    private final BasicLoginService basicLoginService;

    public PinpointBasicLoginConfig(BasicLoginService basicLoginService) {
        this.basicLoginService = Objects.requireNonNull(basicLoginService, "basicLoginService");
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);

        auth.eraseCredentials(false);
        auth.userDetailsService(basicLoginService.getUserDetailsService());
        return auth.build();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // for common
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin()
                .successHandler(new SaveJwtTokenAuthenticationSuccessHandler(basicLoginService))
                .and()
                .httpBasic()
                .and()
                .logout()
                .deleteCookies(BasicLoginConstants.PINPOINT_JWT_COOKIE_NAME);

        // for admin
        http.authorizeHttpRequests().requestMatchers(antMatcher("/admin/**")).hasRole("ADMIN")
                .and()
                .exceptionHandling()
                .accessDeniedPage(BasicLoginConstants.URI_NOT_AUTHORIZED);

        // for user
        http.authorizeHttpRequests().anyRequest().authenticated();

        http.addFilterBefore(new JwtRequestFilter(basicLoginService), UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(new PreAuthenticationCheckFilter(), DefaultLoginPageGeneratingFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}