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

import com.navercorp.pinpoint.login.basic.config.BasicLoginProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
@Service
public class PinpointMemoryUserDetailsService implements UserDetailsService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Map<String, UserDetails> userDetailsMap;

    public PinpointMemoryUserDetailsService(BasicLoginProperties basicLoginConfig) {
        Map<String, UserDetails> userDetailsMap = new HashMap<>();

        final List<UserDetails> userList = basicLoginConfig.getUserList();

        for (UserDetails user : userList) {
            userDetailsMap.put(user.getUsername(), user);
        }

        if (logger.isDebugEnabled()) {
            Collection<String> userRoleUserNameList = userList.stream().map(UserDetails::getUsername).collect(Collectors.toList());
            logger.debug("Has been registered {} that has USER role.", userRoleUserNameList);
        }

        List<UserDetails> adminList = basicLoginConfig.getAdminList();
        for (UserDetails admin : adminList) {
            userDetailsMap.put(admin.getUsername(), admin);
        }

        if (logger.isDebugEnabled()) {
            Collection<String> adminRoleUserNameList = adminList.stream().map(UserDetails::getUsername).collect(Collectors.toList());
            logger.debug("Has been registered {} that has ADMIN role.", adminRoleUserNameList);
        }

        this.userDetailsMap = Map.copyOf(userDetailsMap);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDetailsMap.get(username);
    }

}