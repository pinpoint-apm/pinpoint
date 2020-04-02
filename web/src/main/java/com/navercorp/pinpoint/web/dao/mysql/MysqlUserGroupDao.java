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
package com.navercorp.pinpoint.web.dao.mysql;

import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.web.vo.UserPhoneInfo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlUserGroupDao implements UserGroupDao {
    
    private static final String NAMESPACE = UserGroupDao.class.getPackage().getName() + "." + UserGroupDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlUserGroupDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public String createUserGroup(UserGroup userGroup) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUserGroup", userGroup);
        return userGroup.getNumber();
    }

    @Override
    public List<UserGroup> selectUserGroup() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUserGroupList");
    }

    @Override
    public boolean isExistUserGroup(String userGroupId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "isExistUserGroup", userGroupId);
    }

    @Override
    public List<UserPhoneInfo> selectPhoneInfoOfMember(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectPhoneInfoOfMember", userGroupId);
    }

    @Override
    public List<UserGroup> selectUserGroupByUserId(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUserGroupListByUserId", userId);
    }
    
    @Override
    public List<UserGroup> selectUserGroupByUserGroupId(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUserGroupByUserGroupId", userGroupId);
    }

    @Override
    public void updateUserGroup(UserGroup userGroup) {
        sqlSessionTemplate.update(NAMESPACE + "updateUserGroup", userGroup);
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteUserGroup", userGroup);
    }

    @Override
    public void insertMember(UserGroupMember userGroupMember) {
        sqlSessionTemplate.insert(NAMESPACE + "insertMember", userGroupMember);
    }

    @Override
    public void deleteMember(UserGroupMember userGroupMember) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteMember", userGroupMember);
    }

    @Override
    public List<UserGroupMember> selectMember(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectMemberList", userGroupId);
    }

    @Override
    public void updateMember(UserGroupMember userGroupMember) {
        sqlSessionTemplate.update(NAMESPACE + "updateMember", userGroupMember);
    }
    
    @Override
    public void deleteMemberByUserGroupId(String userGroupId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteMemberByUserGroupId", userGroupId);
    }

    @Override
    public List<String> selectPhoneNumberOfMember(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectPhoneNumberOfMember", userGroupId);
    }

    @Override
    public List<String> selectEmailOfMember(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectEmailOfMember", userGroupId);
    }

    @Override
    public void updateUserGroupIdOfMember(UserGroup userGroup) {
        sqlSessionTemplate.update(NAMESPACE + "updateUserGroupIdOfMember", userGroup);
    }
}
