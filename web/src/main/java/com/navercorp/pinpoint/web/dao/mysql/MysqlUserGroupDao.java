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

import javax.xml.stream.events.Namespace;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlUserGroupDao implements UserGroupDao {
    
    private static final String NAMESPACE = UserGroupDao.class.getPackage().getName() + "." + UserGroupDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public void createUserGroup(String userGroupId) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUserGroup", userGroupId);
    }

    @Override
    public List<String> selectUserGroupList() {
        return null;
    }

    @Override
    public void updateUserGroup() {
    }

    @Override
    public void deleteUserGroup(String userGroupId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteUserGroup", userGroupId);
    }

    @Override
    public void insertMember(UserGroupMember userGroupMember) {
        sqlSessionTemplate.insert(NAMESPACE + "insertMember", userGroupMember);
    }

    @Override
    public void deleteMember(UserGroupMember userGroupMember) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteMember", userGroupMember);
    }

}
