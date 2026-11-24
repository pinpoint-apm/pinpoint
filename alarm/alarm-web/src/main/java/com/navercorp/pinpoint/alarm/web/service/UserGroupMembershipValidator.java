/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.alarm.web.service;

import com.navercorp.pinpoint.user.service.UserGroupService;
import com.navercorp.pinpoint.user.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
public class UserGroupMembershipValidator {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final UserService userService;
    // The user group service reaches back to the alarm services through its deletion listener,
    // so it is resolved on use instead of at construction.
    private final ObjectProvider<UserGroupService> userGroupService;
    private final boolean enabled;

    public UserGroupMembershipValidator(UserService userService,
                                        ObjectProvider<UserGroupService> userGroupService,
                                        @Value("${alarm.channel.member-check.enabled:false}") boolean enabled) {
        this.userService = Objects.requireNonNull(userService, "userService");
        this.userGroupService = Objects.requireNonNull(userGroupService, "userGroupService");
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void validate(String userGroupId) {
        if (!enabled || isAdmin()) {
            return;
        }
        String userId = userService.getUserIdFromSecurity();
        if (!StringUtils.hasText(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No login user to check the user group. Set alarm.channel.member-check.enabled=false to allow any group");
        }
        if (!userGroupService.getObject().checkValid(userId, userGroupId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a member of user group");
        }
    }

    private static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ADMIN_AUTHORITY::equals);
    }
}
