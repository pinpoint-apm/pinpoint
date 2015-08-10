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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value = "/userGroup")
public class UserGroupController {
    
    public static final String USER_GROUP_ID = "userGroupId";

    @Autowired
    UserGroupService userGroupService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> createUserGroup(@RequestBody UserGroup userGroup) {
        if (StringUtils.isEmpty(userGroup.getId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not id of userGroup in params to create user group");
            return result;
        }
        
        String userGroupNumber = userGroupService.createUserGroup(userGroup);

        Map<String, String> result = new HashMap<String, String>();
        result.put("number", userGroupNumber);
        return result;
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroup(@RequestBody UserGroup userGroup) {
        if (StringUtils.isEmpty(userGroup.getId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is id of userGroup in params to delete user group");
            return result;
        }
        
        userGroupService.deleteUserGroup(userGroup);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<UserGroup> getUserGroup() {
        return userGroupService.selectUserGroup();
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserGroup(@RequestBody UserGroup userGroup) {
        if (StringUtils.isEmpty(userGroup.getNumber()) || StringUtils.isEmpty(userGroup.getId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not id or number of user group in params to update user group");
            return result;
        }
        
        userGroupService.updateUserGroup(userGroup);
        
        Map<String, String> result = new HashMap<String, String>();
        
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(value = "/member", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertUserGroupMember(@RequestBody UserGroupMember userGroupMember) {
        if (StringUtils.isEmpty(userGroupMember.getMemberId()) || StringUtils.isEmpty(userGroupMember.getMemberId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId or memberId in params to insert user group member");
            return result;
        }
        
        String numOfMember = userGroupService.insertMember(userGroupMember);

        Map<String, String> result = new HashMap<String, String>();
        result.put("number", numOfMember);
        return result;
    }
    
    @RequestMapping(value = "/member", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteUserGroupMember(@RequestBody UserGroupMember userGroupMember) {
        
        if (StringUtils.isEmpty(userGroupMember.getMemberId()) || StringUtils.isEmpty(userGroupMember.getMemberId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId or memberId in params to delete user group member");
            return result;
        }
        
        userGroupService.deleteMember(userGroupMember);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(value = "/member", method = RequestMethod.GET)
    @ResponseBody
    public List<UserGroupMember> getUserGroupMember(@RequestBody Map<String, String> params) {
        String userGroupId = params.get(USER_GROUP_ID);
        
        //need param check and make respose message for exception
        
        return userGroupService.selectMember(userGroupId);
    }
    
    @RequestMapping(value = "/member", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateUserGroupMember(@RequestBody UserGroupMember userGroupMember) {
        if (StringUtils.isEmpty(userGroupMember.getNumber()) || StringUtils.isEmpty(userGroupMember.getMemberId()) || StringUtils.isEmpty(userGroupMember.getMemberId())) {
            Map<String, String> result = new HashMap<String, String>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not number/userGroupId/memberId in params to update user group member");
            return result;
        }
        
        userGroupService.updateMember(userGroupMember);

        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "SUCCESS");
        return result;
    }
}
