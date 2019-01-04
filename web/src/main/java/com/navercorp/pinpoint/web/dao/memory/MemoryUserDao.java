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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
@Repository
public class MemoryUserDao implements UserDao {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger userNumGenerator  = new AtomicInteger(); 
    
    @Override
    public void insertUser(User user) {
        String userNumber = String.valueOf(userNumGenerator.getAndIncrement());
        user.setNumber(userNumber);
        users.put(user.getUserId(), user);
    }

    @Override
    public void insertUserList(List<User> users) {
        for (User user : users) {
            String userNumber = String.valueOf(userNumGenerator.getAndIncrement());
            user.setNumber(userNumber);
            this.users.put(user.getUserId(), user);
        }
    }

    @Override
    public void deleteUser(String userId) {
        users.remove(userId);
    }

    @Override
    public List<User> selectUser() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void updateUser(User user) {
        users.put(user.getUserId(), user);
    }

    @Override
    public boolean isExistUserId(String userId) {
        return users.containsKey(userId);
    }

    @Override
    public User selectUserByUserId(String userId) {
        return users.get(userId);
    }

    @Override
    public List<User> selectUserByDepartment(String department) {
        List<User> userList = new LinkedList<>();

        for (User user : users.values()) {
            if (department.equals(user.getDepartment())) {
                userList.add(user);
            }
        }
        
        return userList;
    }

    @Override
    public List<User> selectUserByUserName(String userName) {
        List<User> userList = new LinkedList<>();

        for (User user : users.values()) {
            if (userName.equals(user.getName())) {
                userList.add(user);
            }
        }
        
        return userList;
    }

    @Override
    public void dropAndCreateUserTable() {
        users.clear();
        userNumGenerator.lazySet(1);
    }
}
