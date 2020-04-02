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

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlUserDao implements UserDao {

    private static final String NAMESPACE = UserDao.class.getPackage().getName() + "." + UserDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlUserDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public void insertUser(User user) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUser", user);
    }

    @Override
    public void insertUserList(List<User> users) {
        sqlSessionTemplate.insert(NAMESPACE + "insertUserList", users);
    }

    @Override
    public void deleteUser(String userId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteUser", userId);
        
    }

    @Override
    public List<User> selectUser() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUserList"); 
    }

    @Override
    public void updateUser(User user) {
        sqlSessionTemplate.update(NAMESPACE + "updateUser", user);
    }

    @Override
    public boolean isExistUserId(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "isExistUserId", userId);
    }

    @Override
    public User selectUserByUserId(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectUserByUserId", userId);
    }

    @Override
    public List<User> selectUserByDepartment(String department) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUserByDepartment", department);
    }

    @Override
    public List<User> selectUserByUserName(String userName) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectUserByUserName", userName);
    }

    @Override
    public void dropAndCreateUserTable() {
        sqlSessionTemplate.selectOne(NAMESPACE + "dropAndCreateUserTable");
        
    }
}
