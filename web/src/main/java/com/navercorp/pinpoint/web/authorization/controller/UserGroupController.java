/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.response.CreateUserGroupResponse;
import com.navercorp.pinpoint.web.response.Response;
import com.navercorp.pinpoint.web.response.SuccessResponse;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.util.ValueValidator;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import com.navercorp.pinpoint.web.vo.UserGroupMemberParam;
import com.navercorp.pinpoint.web.vo.exception.PinpointUserGroupException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping(value = "/userGroup")
public class UserGroupController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String USER_GROUP_ID = "userGroupId";
    public static final String USER_ID = "userId";

    private final UserGroupService userGroupService;

    public UserGroupController(UserGroupService userGroupService) {
        this.userGroupService = Objects.requireNonNull(userGroupService, "userGroupService");
    }

    @PostMapping()
    public ResponseEntity<Response> createUserGroup(@RequestBody UserGroup userGroup) {
        if (!ValueValidator.validateUserGroupId(userGroup.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usergroupId pattern is invalid to create user group");
        }

        try {
            String userGroupNumber = userGroupService.createUserGroup(userGroup);
            return ResponseEntity.ok(new CreateUserGroupResponse("SUCCESS", userGroupNumber));
        } catch (PinpointUserGroupException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping()
    public ResponseEntity<Response> deleteUserGroup(@RequestBody UserGroup userGroup) {
        if (StringUtils.isEmpty(userGroup.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is id of userGroup in params to delete user group");
        }

        try {
            userGroupService.deleteUserGroup(userGroup);
            return SuccessResponse.ok();
        } catch (PinpointUserGroupException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping()
    public List<UserGroup> getUserGroup(@RequestParam(value = USER_ID, required = false) String userId, @RequestParam(value = USER_GROUP_ID, required = false) String userGroupId) {
        if (StringUtils.hasLength(userId)) {
            return userGroupService.selectUserGroupByUserId(userId);
        } else if (StringUtils.hasLength(userGroupId)) {
            return userGroupService.selectUserGroupByUserGroupId(userGroupId);
        }
        return userGroupService.selectUserGroup();
    }

    @PostMapping(value = "/member")
    public ResponseEntity<Response> insertUserGroupMember(@RequestBody UserGroupMemberParam userGroupMember) {
        if (StringUtils.isEmpty(userGroupMember.getMemberId()) || StringUtils.isEmpty(userGroupMember.getUserGroupId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "there is not userGroupId or memberId in params to insert user group member");
        }
        userGroupService.insertMember(userGroupMember);
        return SuccessResponse.ok();
    }

    @DeleteMapping(value = "/member")
    public ResponseEntity<Response> deleteUserGroupMember(@RequestBody UserGroupMemberParam userGroupMember) {
        if (StringUtils.isEmpty(userGroupMember.getUserGroupId()) || StringUtils.isEmpty(userGroupMember.getMemberId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not userGroupId or memberId in params to delete user group member");
        }
        userGroupService.deleteMember(userGroupMember);
        return SuccessResponse.ok();
    }

    @GetMapping(value = "/member")
    public List<UserGroupMember> getUserGroupMember(@RequestParam(USER_GROUP_ID) String userGroupId) {
        return userGroupService.selectMember(userGroupId);
    }
}
