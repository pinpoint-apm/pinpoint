/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.login.basic.config;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class BasicLoginProperties implements InitializingBean {

    private static final String DEFAULT_JWT_SECRET_KEY = "PINPOINT_JWT_SECRET";

    private static final long DEFAULT_EXPIRATION_TIME_SECONDS = TimeUnit.HOURS.toSeconds(12);

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${web.security.auth.user:}")
    private List<String> userIdAndPasswordPairList;

    @Value("${web.security.auth.admin:}")
    private List<String> adminIdAndPasswordPairList;

    @Value("${web.security.auth.jwt.secretkey:#{null}}")
    private String jwtSecretKey = DEFAULT_JWT_SECRET_KEY;

    private List<UserDetails> userList;

    private List<UserDetails> adminList;

    public List<UserDetails> getUserList() {
        return userList;
    }

    public List<UserDetails> getAdminList() {
        return adminList;
    }

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public long getExpirationTimeSeconds() {
        return DEFAULT_EXPIRATION_TIME_SECONDS;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.userList = createUser(userIdAndPasswordPairList);

        this.adminList = createAdmin(adminIdAndPasswordPairList);

        Assert.hasLength(jwtSecretKey, "jwtSecretKey must not be empty");
    }

    private List<UserDetails> createUser(List<String> idAndPasswordList) {
        return createUserDetails(idAndPasswordList, "ROLE_USER");
    }

    private List<UserDetails> createAdmin(List<String> idAndPasswordList) {
        return createUserDetails(idAndPasswordList, "ROLE_ADMIN");
    }

    private List<UserDetails> createUserDetails(List<String> idAndPasswordList, String role) {
        if (CollectionUtils.isEmpty(idAndPasswordList)) {
            return List.of();
        }

        List<UserDetails> users = new ArrayList<>(idAndPasswordList.size());

        for (String idAndPassword : idAndPasswordList) {
            List<String> tokenize = StringUtils.tokenizeToStringList(idAndPassword, ":");
            if (CollectionUtils.nullSafeSize(tokenize) == 2) {
                String id = tokenize.get(0);
                String password = tokenize.get(1);

                User role_user = new User(id, passwordEncoder.encode(password), List.of(new SimpleGrantedAuthority(role)));
                users.add(role_user);
            }
        }
        return users;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BasicLoginConfig{");
        sb.append("userList=").append(userList);
        sb.append(", adminList=").append(adminList);
        sb.append('}');
        return sb.toString();
    }

}