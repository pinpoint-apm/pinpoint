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
package com.navercorp.pinpoint.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.web.util.ValueValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public final static String USER_ID = "userid";
    
    @Autowired
    UserService userService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUser(@RequestBody User user) {
        if(ValueValidator.validateUser(user) == false) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "User information validation failed to creating user infomation.");
            return result;
        }
        
        userService.insertUser(user);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deletetUser(@RequestBody User user) {
        if (StringUtils.isEmpty(user.getUserId())) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userId in params to delete user");
            return result;
        }
        
        userService.deleteUser(user.getUserId());

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getUser(@RequestParam(value="userId", required=false) String userId, @RequestParam(value="searchKey", required=false) String searchKey) {
        try {
            if(userId != null) {
                List<User> users = new ArrayList<>(1);
                users.add(userService.selectUserByUserId(userId));
                return users;
            } else if (searchKey != null) {
                List<User> users = userService.selectUserByDepartment(searchKey);
                users.addAll(userService.selectUserByUserName(searchKey));
                return users;
            } else {
                return userService.selectUser();
            }
        } catch (Exception e) {
            logger.error("can't select user", e);
            
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "This api need to collect condition for search.");
            return result;
        }
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUser(@RequestBody User user) {
        if(ValueValidator.validateUser(user) == false) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "User information validation failed to creating user infomation.");
            return result;
        }
        
        userService.updateUser(user);
        
        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, String> handleException(Exception e) {
        logger.error(" Exception occurred while trying to CRUD user information", e);
        
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", "Exception occurred while trying to CRUD user information");
        return result;
    }
}
