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

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import com.navercorp.pinpoint.web.vo.UserPhoneInfo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
@Repository
public class MemoryUserGroupDao implements UserGroupDao {
    private final Map<String, UserGroup> userGroups = new ConcurrentHashMap<>();
    private final Map<String, UserGroupMember> userGroupMembers = new ConcurrentHashMap<>();
    private final Map<String, Rule> alarmRule;

    private final IdGenerator userGroupNumGenerator  = new IdGenerator();
    private final IdGenerator userGroupMemNumGenerator  = new IdGenerator();
    private final UserDao userDao;

    public MemoryUserGroupDao(AlarmRule alarmRuleData, UserDao userDao) {
        Objects.requireNonNull(alarmRuleData, "alarmRuleData");
        this.userDao = Objects.requireNonNull(userDao, "userDao");
        this.alarmRule = alarmRuleData.getAlarmRule();
    }

    @Override
    public String createUserGroup(UserGroup userGroup) {
        String userGroupNumber = userGroupNumGenerator.getId();
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
        
        List<UserGroup> groups = new ArrayList<>();
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
        String memberNum = userGroupMemNumGenerator.getId();
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
        List<UserGroupMember> userGroupMemberList = new ArrayList<>();
        
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
        List<UserGroupMember> userGroupMemberList = new ArrayList<>();
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                userGroupMemberList.add(member);
            }
        }
        
        List<String> phoneNumbers = new ArrayList<>();
        
        for (UserGroupMember member : userGroupMemberList) {
            User user = userDao.selectUserByUserId(member.getMemberId());
            phoneNumbers.add(user.getPhoneNumber());
        }
        
        return phoneNumbers;
    }

    @Override
    public List<String> selectEmailOfMember(String userGroupId) {
        List<UserGroupMember> userGroupMemberList = new ArrayList<>();
        
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                userGroupMemberList.add(member);
            }
        }
        
        List<String> emails = new ArrayList<>();
        
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

    @Override
    public boolean isExistUserGroup(String userGroupId) {
        return userGroups.containsKey(userGroupId);
    }

    @Override
    public boolean isExistUserGroupMember(UserGroupMember userGroupMember) {
        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupMember.getUserGroupId()) &&
                member.getMemberId().equals(userGroupMember.getMemberId()))
                return true;
        }
        return false;
    }

    @Override
    public List<UserPhoneInfo> selectPhoneInfoOfMember(String userGroupId) {
        List<UserGroupMember> userGroupMemberList = new ArrayList<>();

        for (UserGroupMember member : userGroupMembers.values()) {
            if (member.getUserGroupId().equals(userGroupId)) {
                userGroupMemberList.add(member);
            }
        }

        List<UserPhoneInfo> userPhoneInfoList  = new ArrayList<>();

        for (UserGroupMember member : userGroupMemberList) {
            User user = userDao.selectUserByUserId(member.getMemberId());
            userPhoneInfoList.add(new UserPhoneInfo(user.getPhoneCountryCode(), user.getPhoneNumber()));
        }

        return userPhoneInfoList;
    }

    @Override
    public void deleteRuleByUserGroupId(String userGroupId) {
        for (Map.Entry<String, Rule> entry : alarmRule.entrySet()) {
            if (entry.getValue().getUserGroupId().equals(userGroupId)) {
                alarmRule.remove(entry.getKey());
            }
        }
    }
}
