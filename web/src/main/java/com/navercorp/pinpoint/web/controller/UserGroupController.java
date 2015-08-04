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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value = "/userGroup")
public class UserGroupController {
    
    public static final String USER_GROUP_ID = "userGroupId";
    public static final String USER_GROUP_MEMBER_ID = "userGroupMemberId";

    @Autowired
    UserGroupService userGroupService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> createUserGroup(@RequestBody Map<String, String> params) {
        String userGroupId = params.get(USER_GROUP_ID);
        
        if (userGroupId == null) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId in params to creating user group");
            return result;
        }
        
        userGroupService.createUserGroup(userGroupId);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroup(@RequestBody Map<String, String> params) {
        String userGroupId = params.get(USER_GROUP_ID);
        
        if (userGroupId == null) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId in params to deleting user group");
            return result;
        }
        
        userGroupService.deleteUserGroup(userGroupId);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    
    @RequestMapping(value = "/member", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserGroupMember(@RequestBody Map<String, String> params) {
        String userGroupId = params.get(USER_GROUP_ID);
        String userGroupMemberId = params.get(USER_GROUP_MEMBER_ID);
        
        if (userGroupId == null || userGroupMemberId == null) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId or userGroupMemberId in params to deleting user group");
            return result;
        }
        
        userGroupService.insertMember(new UserGroupMember(userGroupId, userGroupMemberId));

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(value = "/member", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroupMember(@RequestBody Map<String, String> params) {
        String userGroupId = params.get(USER_GROUP_ID);
        String userGroupMemberId = params.get(USER_GROUP_MEMBER_ID);
        
        if (userGroupId == null || userGroupMemberId == null) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId or userGroupMemberId in params to deleting user group");
            return result;
        }
        
        userGroupService.deleteMember(new UserGroupMember(userGroupId, userGroupMemberId));

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
}
