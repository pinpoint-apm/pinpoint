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

import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
public interface UserService {

    void dropAndCreateUserTable();
    
    void insertUser(User user);

    void insertUserList(List<User> users);

    void deleteUser(String userId);

    void updateUser(User user);

    List<User> selectUser();

    User selectUserByUserId(String userId);

    List<User> selectUserByUserName(String userName);

    List<User> selectUserByDepartment(String department);

    boolean isExistUserId(String userId);

    String getUserIdFromSecurity();
}
