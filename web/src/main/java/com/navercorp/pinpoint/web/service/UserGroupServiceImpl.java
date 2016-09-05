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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Service
public class UserGroupServiceImpl implements UserGroupService {

    @Autowired
    UserGroupDao userGroupDao;
    
    @Override
    public String createUserGroup(UserGroup userGroup) {
        return userGroupDao.createUserGroup(userGroup);
    }

    @Override
    public List<UserGroup> selectUserGroup() {
        return userGroupDao.selectUserGroup();
    }

    @Override
    public List<UserGroup> selectUserGroupByUserId(String userId) {
        return userGroupDao.selectUserGroupByUserId(userId);
    }

    @Override
    public List<UserGroup> selectUserGroupByUserGroupId(String userGroupId) {
        return userGroupDao.selectUserGroupByUserGroupId(userGroupId);
    }
    
    @Override
    public void updateUserGroup(UserGroup userGroup) {
        userGroupDao.updateUserGroup(userGroup);
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) {
        userGroupDao.deleteUserGroup(userGroup);
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
    public List<UserGroupMember> selectMember(String userGroupId) {
        return userGroupDao.selectMember(userGroupId);
    }

    @Override
    public void updateMember(UserGroupMember userGroupMember) {
        userGroupDao.updateMember(userGroupMember);
    }
    
    @Override
    public List<String> selectPhoneNumberOfMember(String userGroupId) {
        return userGroupDao.selectPhoneNumberOfMember(userGroupId);
    }

    @Override
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
