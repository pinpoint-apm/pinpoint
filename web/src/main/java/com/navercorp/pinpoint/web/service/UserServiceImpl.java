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

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;
    
    @Override
    public void insertUser(User user) {
        userDao.insertUser(user);
    }

    @Override
    public void deleteUser(User user) {
        userDao.deleteUser(user);
    }


    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @Override
    public List<User> selectUser() {
        return userDao.selectUser();
    }

    @Override
    public User selectUserByUserId(String userId) {
        return userDao.selectUserByUserId(userId);
    }

    @Override
    public List<User> selectUserByUserName(String userName) {
        return userDao.selectUserByUserName(userName);
    }

    @Override
    public List<User> selectUserByDepartment(String department) {
        return userDao.selectUserByDepartment(department);
    }

    @Override
    public void dropAndCreateUserTable() {
        userDao.dropAndCreateUserTable();
    }

}
