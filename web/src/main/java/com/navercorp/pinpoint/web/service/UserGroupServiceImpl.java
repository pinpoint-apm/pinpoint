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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.util.DefaultUserInfoDecoder;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import com.navercorp.pinpoint.web.vo.UserPhoneInfo;
import com.navercorp.pinpoint.web.vo.exception.PinpointUserGroupException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserGroupServiceImpl implements UserGroupService {

    private final UserGroupDao userGroupDao;

    private final UserInfoDecoder userInfoDecoder;

    private final AlarmService alarmService;

    private final ConfigProperties webProperties;

    private final UserService userService;

    public UserGroupServiceImpl(UserGroupDao userGroupDao, Optional<UserInfoDecoder> userInfoDecoder, AlarmService alarmService, ConfigProperties webProperties, UserService userService) {
        this.userGroupDao = Objects.requireNonNull(userGroupDao, "userGroupDao");
        this.userInfoDecoder = Objects.requireNonNull(userInfoDecoder, "userInfoDecoder").orElse(DefaultUserInfoDecoder.EMPTY_USER_INFO_DECODER);
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
        this.webProperties = Objects.requireNonNull(webProperties, "webProperties");
        this.userService = Objects.requireNonNull(userService, "userService");
    }

    @Override
    public String createUserGroup(UserGroup userGroup) throws PinpointUserGroupException {
        if (userGroupDao.isExistUserGroup(userGroup.getId())) {
            throw new PinpointUserGroupException("userGroup's name already exist. :" + userGroup.getId());
        }

        String userGroupNumber = userGroupDao.createUserGroup(userGroup);

        if (webProperties.isOpenSource() == false) {
            String userId = userService.getUserIdFromSecurity();
            if (StringUtils.isEmpty(userId)) {
                throw new PinpointUserGroupException("There is not userId or fail to create userGroup.");
            }

            insertMember(new UserGroupMember(userGroup.getId(), userId));
        }

        return userGroupNumber;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGroup> selectUserGroup() {
        return userGroupDao.selectUserGroup();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGroup> selectUserGroupByUserId(String userId) {
        return userGroupDao.selectUserGroupByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGroup> selectUserGroupByUserGroupId(String userGroupId) {
        return userGroupDao.selectUserGroupByUserGroupId(userGroupId);
    }
    
    @Override
    public void updateUserGroup(UserGroup userGroup) {
        userGroupDao.updateUserGroup(userGroup);
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) throws PinpointUserGroupException {
        userGroupDao.deleteUserGroup(userGroup);
        userGroupDao.deleteMemberByUserGroupId(userGroup.getId());
        alarmService.deleteRuleByUserGroupId(userGroup.getId());
    }

    @Transactional(readOnly = true)
    public boolean checkValid(String userId, String userGroupId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        if (containMemberForUserGroup(userId, userGroupId) == false) {
            return false;
        }

        return true;
    }

    @Override
    public void insertMember(UserGroupMember userGroupMember) {
        userGroupDao.insertMember(userGroupMember);
    }

    @Override
    public void deleteMember(UserGroupMember userGroupMember) {
        userGroupDao.deleteMember(userGroupMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGroupMember> selectMember(String userGroupId) {
        return userGroupDao.selectMember(userGroupId);
    }

    @Override
    public void updateMember(UserGroupMember userGroupMember) {
        userGroupDao.updateMember(userGroupMember);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> selectPhoneNumberOfMember(String userGroupId) {
        final List<String> phoneNumberList = userGroupDao.selectPhoneNumberOfMember(userGroupId);
        List<String> decodedPhoneNumberList = phoneNumberList;

        if (!DefaultUserInfoDecoder.EMPTY_USER_INFO_DECODER.equals(userInfoDecoder)) {
            decodedPhoneNumberList =  userInfoDecoder.decodePhoneNumberList(phoneNumberList);
        }

        return User.removeHyphenForPhoneNumberList(decodedPhoneNumberList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPhoneInfo> selectPhoneInfoOfMember(String userGroupId) {
        final List<UserPhoneInfo> userPhoneInfoList = userGroupDao.selectPhoneInfoOfMember(userGroupId);

        if (CollectionUtils.isEmpty(userPhoneInfoList)) {
            return userPhoneInfoList;
        }

        if (DefaultUserInfoDecoder.EMPTY_USER_INFO_DECODER.equals(userInfoDecoder)) {
            return userPhoneInfoList;
        }


        List<UserPhoneInfo> convertedUserPhoneInfoList = new ArrayList<>(userPhoneInfoList.size());

        for (UserPhoneInfo userPhoneInfo : userPhoneInfoList) {
            String decodedPhoneNumber = userInfoDecoder.decodePhoneNumber(userPhoneInfo.getPhoneNumber());
            String phoneNumber = User.removeHyphenForPhoneNumber(decodedPhoneNumber);
            convertedUserPhoneInfoList.add(new UserPhoneInfo(userPhoneInfo.getPhoneCountryCode(), phoneNumber));
        }

        return convertedUserPhoneInfoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> selectEmailOfMember(String userGroupId) {
        return userGroupDao.selectEmailOfMember(userGroupId);
    }

    @Override
    public void updateUserGroupIdOfMember(UserGroup userGroup) {
        userGroupDao.updateUserGroupIdOfMember(userGroup);
    }

    private boolean containMemberForUserGroup(String userId, String userGroupId) {
        List<UserGroupMember> memberList = userGroupDao.selectMember(userGroupId);
        for (UserGroupMember member : memberList) {
            if(member.getMemberId().equals(userId)) {
                return true;
            }
        }
        
        return false;
    }
}
