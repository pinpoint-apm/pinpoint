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

import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.AlarmService;

/**
 * @author minwoo.jung
 */
@Controller
@RequestMapping(value={"/alarmRule", "/application/alarmRule"})
public class AlarmController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public final static String USER_GROUP_ID = "userGroupId";
    public final static String APPLICATION_ID = "applicationId";

    @Autowired
    AlarmService alarmService;
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertRule(@RequestBody Rule rule) {
        Map<String, String> result = new HashMap<>();

        if (StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId()) || StringUtils.isEmpty(rule.getThreshold())) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
            return result;
        }
        
        String ruleId = alarmService.insertRule(rule);

        result.put("result", "SUCCESS");
        result.put("ruleId", ruleId);
        return result;
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId())) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not ruleId to delete alarm rule");
            return result;
        }
        
        alarmService.deleteRule(rule);

        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getRule(@RequestParam(value=USER_GROUP_ID, required=false) String userGroupId, @RequestParam(value=APPLICATION_ID, required=false) String applicationId) {
        if (StringUtils.isEmpty(userGroupId) && StringUtils.isEmpty(applicationId)) {
            Map<String, String> result = new HashMap<>();
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not userGroupId or applicationID to get alarm rule");
            return result;
        }
        
        if (!StringUtils.isEmpty(userGroupId)) {
            return alarmService.selectRuleByUserGroupId(userGroupId);
        }
        
        return alarmService.selectRuleByApplicationId(applicationId);
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateRule(@RequestBody Rule rule) {
        Map<String, String> result = new HashMap<>();

        if (StringUtils.isEmpty(rule.getRuleId()) || StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId())) {
            result.put("errorCode", "500");
            result.put("errorMessage", "there is not ruleId/userGroupId/applicationid/checkerName to get alarm rule");
            return result;
        }
        
        alarmService.updateRule(rule);
        
        result.put("result", "SUCCESS");
        return result;
    }
    
    @RequestMapping(value = "/checker", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getCheckerName() {
        return CheckerCategory.getNames();
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String, String> handleException(Exception e) {
        logger.error(" Exception occurred while trying to CRUD Alarm Rule information", e);
        
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", "500");
        result.put("errorMessage", "Exception occurred while trying to Alarm Rule information");
        return result;
    }
    
}
