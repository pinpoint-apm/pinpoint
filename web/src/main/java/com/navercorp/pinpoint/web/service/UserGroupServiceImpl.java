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

import java.util.List;

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.util.DefaultUserInfoDecoder;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroupMemberParam;
import com.navercorp.pinpoint.web.vo.exception.PinpointUserGroupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserGroupServiceImpl implements UserGroupService {

    @Autowired
    UserGroupDao userGroupDao;

    @Autowired(required = false)
    UserInfoDecoder userInfoDecoder = DefaultUserInfoDecoder.EMPTY_USER_INFO_DECODER;

    @Autowired
    AlarmService alarmService;

    @Autowired
    private ConfigProperties webProperties;


    
    @Override
    public String createUserGroup(UserGroup userGroup, String userId) throws PinpointUserGroupException {
        String userGroupNumber = userGroupDao.createUserGroup(userGroup);

        if (webProperties.isOpenSource() == false) {
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
    public void deleteUserGroup(UserGroup userGroup, String userId) throws PinpointUserGroupException {
        if (webProperties.isOpenSource() == false) {
            if (checkValid(userId, userGroup.getId()) == false) {
                throw new PinpointUserGroupException("There is not userId or you don't have authoriy for user group.");
            }
        }

        userGroupDao.deleteUserGroup(userGroup);
        deleteMemberByUserGroupId(userGroup.getId());
        alarmService.deleteRuleByUserGroupId(userGroup.getId());
    }

    private boolean checkValid(String userId, String userGroupId) {
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
    public void insertMemberWithCheckAuthority(UserGroupMemberParam userGroupMember, String userId) throws PinpointUserGroupException {
        if (webProperties.isOpenSource() == false) {
            boolean isValid = checkValid(userId, userGroupMember.getUserGroupId());
            if (isValid == false) {
                throw new PinpointUserGroupException("there is not userId or you don't have authority for user group.");
            }
        }

        insertMember(userGroupMember);
    }


    @Override
    public void deleteMemberWithCheckAuthority(UserGroupMember userGroupMember, String userId) throws PinpointUserGroupException {
        if (webProperties.isOpenSource() == false) {
            boolean isValid = checkValid(userId, userGroupMember.getUserGroupId());
            if (isValid == false) {
                throw new PinpointUserGroupException("there is not userId or you don't have authority for user group.");
            }
        }

        userGroupDao.deleteMember(userGroupMember);
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
    public List<String> selectEmailOfMember(String userGroupId) {
        return userGroupDao.selectEmailOfMember(userGroupId);
    }

    @Override
    public void deleteMemberByUserGroupId(String userGroupId) {
        userGroupDao.deleteMemberByUserGroupId(userGroupId);
    }

    @Override
    public void updateUserGroupIdOfMember(UserGroup userGroup) {
        userGroupDao.updateUserGroupIdOfMember(userGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean containMemberForUserGroup(String userId, String userGroupId) {
        List<UserGroupMember> memberList = userGroupDao.selectMember(userGroupId);
        for (UserGroupMember member : memberList) {
            if(member.getMemberId().equals(userId)) {
                return true;
            }
        }
        
        return false;
    }
}
