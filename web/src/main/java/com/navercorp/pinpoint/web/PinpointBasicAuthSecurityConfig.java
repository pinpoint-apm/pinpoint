/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Taejin Koo
 */
@Configuration
@Profile("basicAuth")
@EnableWebSecurity
public class PinpointBasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String KEY_AUTH_USER_LIST = "web.security.auth.user";
    private static final String KEY_AUTH_ADMIN_LIST = "web.security.auth.admin";

    @Resource(name = "pinpointWebProps")
    private Properties myProps;

    @Autowired
    private Environment environment;

    private String getProperty(String key) {
        final String property = environment.getProperty(key);
        if (StringUtils.hasText(property)) {
            return property;
        }

        return myProps.getProperty(key);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> memoryUserManagerConfigurer = auth.inMemoryAuthentication();

        String userProperty = getProperty(KEY_AUTH_USER_LIST);
        List<UserDetails> users = createUser(userProperty);
        for (UserDetails user : users) {
            memoryUserManagerConfigurer.withUser(user);
        }

        if (logger.isDebugEnabled()) {
            Collection<String> userRoleUserNameList = users.stream().map(user -> user.getUsername()).collect(Collectors.toList());
            logger.debug("Has been registered {} that has USER role.", userRoleUserNameList);
        }

        String adminProperty = getProperty(KEY_AUTH_ADMIN_LIST);
        List<UserDetails> admins = createAdmin(adminProperty);
        for (UserDetails admin : admins) {
            memoryUserManagerConfigurer.withUser(admin);
        }

        if (logger.isDebugEnabled()) {
            Collection<String> adminRoleUserNameList = admins.stream().map(user -> user.getUsername()).collect(Collectors.toList());
            logger.debug("Has been registered {} that has ADMIN role.", adminRoleUserNameList);
        }
    }

    private List<UserDetails> createUser(String userProperty) {
        if (StringUtils.isEmpty(userProperty)) {
            return Collections.emptyList();
        }

        String[] result = Stream.of(userProperty.split(",")).map(String::trim).toArray(String[]::new);
        List<UserDetails> users = createUserDetails(result, "ROLE_USER");
        return users;
    }

    private List<UserDetails> createAdmin(String adminProperty) {
        if (StringUtils.isEmpty(adminProperty)) {
            return Collections.emptyList();
        }

        String[] result = Stream.of(adminProperty.split(",")).map(String::trim).toArray(String[]::new);
        List<UserDetails> users = createUserDetails(result, "ROLE_ADMIN");
        return users;
    }

    private List<UserDetails> createUserDetails(String[] userInfosString, String role) {
        if (ArrayUtils.isEmpty(userInfosString)) {
            return Collections.emptyList();
        }

        List<UserDetails> users = new ArrayList<>(ArrayUtils.getLength(userInfosString));

        for (String s : userInfosString) {
            List<String> strings = StringUtils.tokenizeToStringList(s, ":");
            if (CollectionUtils.nullSafeSize(strings) == 2) {
                String id = strings.get(0);
                String password = strings.get(1);

                User role_user = new User(id, passwordEncoder().encode(password), Arrays.asList(new SimpleGrantedAuthority(role)));

                users.add(role_user);
            }
        }
        return users;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN")
                .and()
                    .formLogin().permitAll()
                .and()
                    .exceptionHandling().accessDeniedPage("/not_authorized.html");

        http.authorizeRequests().anyRequest().authenticated()
                .and()
                    .formLogin()
                .and()
                    .httpBasic();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}