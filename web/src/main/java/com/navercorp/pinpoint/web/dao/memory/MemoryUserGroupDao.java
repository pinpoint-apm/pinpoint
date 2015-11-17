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
package com.navercorp.pinpoint.web.dao.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Repository
public class MemoryUserGroupDao implements UserGroupDao {
    
    private final Map<String, UserGroup> userGroups = new ConcurrentHashMap<>();
    private final Map<String, UserGroupMember> userGroupMembers = new ConcurrentHashMap<>();
    
    private final AtomicInteger userGroupNumGenerator  = new AtomicInteger();
    private final AtomicInteger userGroupMemNumGenerator  = new AtomicInteger();
    
    @Autowired
    UserDao userDao;
    
    @Override
    public String createUserGroup(UserGroup userGroup) {
        String userGroupNumber = String.valueOf(userGroupNumGenerator.getAndIncrement());
        userGroup.setNumber(userGroupNumber);
        userGroups.put(userGroupNumber, userGroup);
        return userGroup.getNumber();
    }

    @Override
    public List<UserGroup> selectUserGroup() {
        return new ArrayList<>(userGroups.values());
    }
    

    @Override
    public List<UserGroup> selectUserGroupByUserId(String userId) {
        Set<String> userGroupNames = new HashSet<>();
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getMemberId().equals(userId)) {
                userGroupNames.add(member.getUserGroupId());
            }
        }
        
        List<UserGroup> groups = new LinkedList<>();
        for(UserGroup userGroup : userGroups.values()) {
            if (userGroupNames.contains(userGroup.getId())) {
                groups.add(userGroup);
            }
        }
        
        return groups;
    }
    
    @Override
    public List<UserGroup> selectUserGroupByUserGroupId(String userGroupId) {
        List<UserGroup> userGroupList = new ArrayList<>();
        
        for(UserGroup userGroup : userGroups.values()) {
            if (userGroup.getId().contains(userGroupId)) {
                userGroupList.add(userGroup);
            }
        }
        
        return userGroupList;
    }

    @Override
    public void updateUserGroup(UserGroup userGroup) {
        userGroups.put(userGroup.getNumber(), userGroup);
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) {
        for(UserGroup ug : userGroups.values()) {
            if (ug.getId().equals(userGroup.getId())) {
                userGroups.remove(ug.getNumber());
                break;
            }
        }
    }

    @Override
    public void insertMember(UserGroupMember userGroupMember) {
        String memberNum = String.valueOf(userGroupMemNumGenerator.getAndIncrement());
        userGroupMember.setNumber(memberNum);
        userGroupMembers.put(memberNum, userGroupMember);
    }

    @Override
    public void deleteMember(UserGroupMember userGroupMember) {
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupMember.getUserGroupId()) && member.getMemberId().equals(userGroupMember.getMemberId())) {
                userGroupMembers.remove(member.getNumber());
                break;
            }
        }
    }

    @Override
    public List<UserGroupMember> selectMember(String userGroupId) {
        List<UserGroupMember> userGroupMemberList = new LinkedList<>();
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                User user = userDao.selectUserByUserId(member.getMemberId());
                member.setName(user.getName());
                member.setDepartment(user.getDepartment());
                userGroupMemberList.add(member);
            }
        }
        
        return userGroupMemberList;
    }

    @Override
    public void updateMember(UserGroupMember userGroupMember) {
        userGroupMembers.put(userGroupMember.getNumber(), userGroupMember);
    }
    
    @Override
    public void deleteMemberByUserGroupId(String userGroupId) {
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                userGroupMembers.remove(userGroupId);
            }
        }
    }

    @Override
    public List<String> selectPhoneNumberOfMember(String userGroupId) {
        List<UserGroupMember> userGroupMemberList = new LinkedList<>();
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                userGroupMemberList.add(member);
            }
        }
        
        List<String> phoneNumbers = new LinkedList<>();
        
        for (UserGroupMember member : userGroupMemberList) {
            User user = userDao.selectUserByUserId(member.getMemberId());
            phoneNumbers.add(user.getPhoneNumber());
        }
        
        return phoneNumbers;
    }

    @Override
    public List<String> selectEmailOfMember(String userGroupId) {
        List<UserGroupMember> userGroupMemberList = new LinkedList<>();
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                userGroupMemberList.add(member);
            }
        }
        
        List<String> emails = new LinkedList<>();
        
        for (UserGroupMember member : userGroupMemberList) {
            User user = userDao.selectUserByUserId(member.getMemberId());
            emails.add(user.getEmail());
        }
        
        return emails;
    }

    @Override
    public void updateUserGroupIdOfMember(UserGroup userGroup) {
        UserGroup group = userGroups.get(userGroup.getNumber());
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(group.getId())) {
                member.setUserGroupId(userGroup.getId());
            }
        }
    }
}
