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

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.util.DefaultUserInfoDecoder;
import com.navercorp.pinpoint.web.util.DefaultUserInfoEncoder;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.util.UserInfoEncoder;
import com.navercorp.pinpoint.web.vo.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class UserServiceImpl implements UserService {

    private static final String EMPTY = "";

    private final UserDao userDao;

    private final UserInfoDecoder userInfoDecoder;
    private final UserInfoEncoder userInfoEncoder;

    public UserServiceImpl(UserDao userDao, Optional<UserInfoDecoder> userInfoDecoder, Optional<UserInfoEncoder> userInfoEncoder) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
        this.userInfoDecoder = Objects.requireNonNull(userInfoDecoder, "userInfoDecoder").orElse(DefaultUserInfoDecoder.EMPTY_USER_INFO_DECODER);
        this.userInfoEncoder = Objects.requireNonNull(userInfoEncoder, "userInfoEncoder").orElse(DefaultUserInfoEncoder.EMPTY_USER_INFO_ENCODER);
    }
    
    @Override
    public void insertUser(User user) {
        User encodedUser = userInfoEncoder.encodeUserInfo(user);
        userDao.insertUser(encodedUser);
    }

    @Override
    public void deleteUser(String userId) {
        userDao.deleteUser(userId);
    }


    @Override
    public void updateUser(User user) {
        User encodedUser = userInfoEncoder.encodeUserInfo(user);
        userDao.updateUser(encodedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> selectUser() {
        List<User> userList = userDao.selectUser();
        return userInfoDecoder.decodeUserInfoList(userList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUser(String condition) {
        List<User> userList = userDao.searchUser(condition);
        return userInfoDecoder.decodeUserInfoList(userList);
    }

    @Override
    @Transactional(readOnly = true)
    public User selectUserByUserId(String userId) {
        User user = userDao.selectUserByUserId(userId);
        return userInfoDecoder.decodeUserInfo(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> selectUserByUserGroupId(String userGroupId) {
        List<User> userList = userDao.selectUserByUserGroupId(userGroupId);
        return userInfoDecoder.decodeUserInfoList(userList);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> selectUserByUserName(String userName) {
        List<User> userList = userDao.selectUserByUserName(userName);
        return userInfoDecoder.decodeUserInfoList(userList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> selectUserByDepartment(String department) {
        List<User> userList = userDao.selectUserByDepartment(department);
        return userInfoDecoder.decodeUserInfoList(userList);
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
    public void insertUserList(List<User> userList) {
        List<User> encodedUserList = userInfoEncoder.encodeUserInfoList(userList);
        userDao.insertUserList(encodedUserList);
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
