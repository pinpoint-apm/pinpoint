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

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.response.CreateUserGroupResponse;
import com.navercorp.pinpoint.user.service.UserGroupService;
import com.navercorp.pinpoint.web.util.ValueValidator;
import com.navercorp.pinpoint.user.vo.UserGroup;
import com.navercorp.pinpoint.user.vo.UserGroupMember;
import com.navercorp.pinpoint.user.vo.UserGroupMemberParam;
import com.navercorp.pinpoint.user.vo.exception.PinpointUserGroupException;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping(value = "/api/userGroup")
@Validated
public class UserGroupController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String USER_GROUP_ID = "userGroupId";
    public static final String USER_ID = "userId";

    private final UserGroupService userGroupService;

    public UserGroupController(UserGroupService userGroupService) {
        this.userGroupService = Objects.requireNonNull(userGroupService, "userGroupService");
    }

    @PostMapping
    public CreateUserGroupResponse createUserGroup(@RequestBody UserGroup userGroup) {
        if (!ValueValidator.validateUserGroupId(userGroup.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userGroupId pattern is invalid to create user group"
            );
        }

        try {
            final String userGroupNumber = userGroupService.createUserGroup(userGroup);
            return new CreateUserGroupResponse(Result.SUCCESS, userGroupNumber);
        } catch (PinpointUserGroupException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping
    public Response deleteUserGroup(@RequestBody UserGroup userGroup) {
        if (StringUtils.isEmpty(userGroup.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "there is id of userGroup in params to delete user group"
            );
        }

        try {
            userGroupService.deleteUserGroup(userGroup);
            return SimpleResponse.ok();
        } catch (PinpointUserGroupException e) {
            logger.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping
    public List<UserGroup> getUserGroups() {
        return userGroupService.selectUserGroup();
    }

    @GetMapping(params = USER_ID)
    public List<UserGroup> getUserGroupOfUser(@RequestParam(USER_ID) String userId) {
        return userGroupService.selectUserGroupByUserId(userId);
    }

    @GetMapping(params = USER_GROUP_ID)
    public List<UserGroup> getUserGroupById(@RequestParam(USER_GROUP_ID) String userGroupId) {
        return userGroupService.selectUserGroupByUserGroupId(userGroupId);
    }

    @PostMapping(value = "/member")
    public Response insertUserGroupMember(@RequestBody UserGroupMemberParam userGroupMember) {
        if (StringUtils.isEmpty(userGroupMember.getMemberId()) ||
                StringUtils.isEmpty(userGroupMember.getUserGroupId())
        ) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "There is no userGroupId or memberId in params to insert user group member"
            );
        }
        userGroupService.insertMember(userGroupMember);
        return SimpleResponse.ok();
    }

    @DeleteMapping(value = "/member")
    public Response deleteUserGroupMember(@RequestBody UserGroupMemberParam userGroupMember) {
        if (StringUtils.isEmpty(userGroupMember.getUserGroupId()) ||
                StringUtils.isEmpty(userGroupMember.getMemberId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is no userGroupId or memberId in params to delete user group member"
            );
        }
        userGroupService.deleteMember(userGroupMember);
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/member")
    public List<UserGroupMember> getUserGroupMember(@RequestParam(USER_GROUP_ID) @NotBlank String userGroupId) {
        return userGroupService.selectMember(userGroupId);
    }

}
