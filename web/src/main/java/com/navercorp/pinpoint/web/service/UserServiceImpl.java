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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.sasl.AuthorizeCallback;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserServiceImpl implements UserService {

    private final String EMPTY = "";

    @Autowired
    UserDao userDao;
    
    @Override
    public void insertUser(User user) {
        userDao.insertUser(user);
    }

    @Override
    public void deleteUser(String userId) {
        userDao.deleteUser(userId);
    }


    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> selectUser() {
        return userDao.selectUser();
    }

    @Override
    @Transactional(readOnly = true)
    public User selectUserByUserId(String userId) {
        return userDao.selectUserByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> selectUserByUserName(String userName) {
        return userDao.selectUserByUserName(userName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> selectUserByDepartment(String department) {
        return userDao.selectUserByDepartment(department);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistUserId(String userId) {
        return userDao.isExistUserId(userId);
    }

    @Override
    public void dropAndCreateUserTable() {
        userDao.dropAndCreateUserTable();
    }

    @Override
    public void insertUserList(List<User> users) {
        userDao.insertUserList(users);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getUserIdFromSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (String)authentication.getPrincipal();
        }

        return EMPTY;
    }

}
