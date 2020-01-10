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

import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import com.navercorp.pinpoint.web.vo.UserPhoneInfo;
import com.navercorp.pinpoint.web.vo.exception.PinpointUserGroupException;

import java.util.List;

/**
 * @author minwoo.jung
 */
public interface UserGroupService {
    String createUserGroup(UserGroup userGroup) throws PinpointUserGroupException;
    
    List<UserGroup> selectUserGroup();
    
    List<UserGroup> selectUserGroupByUserId(String userId);

    List<UserGroup> selectUserGroupByUserGroupId(String userGroupId);

    void updateUserGroup(UserGroup userGroup);

    void deleteUserGroup(UserGroup userGroup) throws PinpointUserGroupException;

    boolean checkValid(String userId, String userGroupId);

    void insertMember(UserGroupMember userGroupMember);

    void deleteMember(UserGroupMember userGroupMember);

    List<UserGroupMember> selectMember(String userGroupId);

    void updateMember(UserGroupMember userGroupMember);

    List<String> selectPhoneNumberOfMember(String userGroupId);

    List<UserPhoneInfo> selectPhoneInfoOfMember(String userGroupId);

    List<String> selectEmailOfMember(String userGroupId);

    void updateUserGroupIdOfMember(UserGroup userGroup);
}
