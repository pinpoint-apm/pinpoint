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

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.security.login.BasicLoginConstants;
import com.navercorp.pinpoint.web.security.login.BasicLoginService;
import com.navercorp.pinpoint.web.security.login.JwtRequestFilter;
import com.navercorp.pinpoint.web.security.login.PreAuthenticationCheckFilter;
import com.navercorp.pinpoint.web.security.login.SaveJwtTokenAuthenticationSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Configuration
@EnableWebSecurity
@Profile("basicLogin")
public class PinpointBasicLoginConfig extends WebSecurityConfigurerAdapter {

    private final BasicLoginService basicLoginService;

    public PinpointBasicLoginConfig(BasicLoginService basicLoginService) {
        this.basicLoginService = Objects.requireNonNull(basicLoginService, "basicLoginService");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.eraseCredentials(false);
        auth.userDetailsService(basicLoginService.getUserDetailsService());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
        http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN")
                .and()
                .exceptionHandling()
                .accessDeniedPage(BasicLoginConstants.URI_NOT_AUTHORIZED);

        // for user
        http.authorizeRequests().anyRequest().authenticated();

        http.addFilterBefore(new JwtRequestFilter(basicLoginService), UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(new PreAuthenticationCheckFilter(), DefaultLoginPageGeneratingFilter.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

} 