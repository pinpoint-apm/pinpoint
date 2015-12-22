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

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.dao.AlarmDao;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context.xml", "classpath:applicationContext-web.xml"})
public class AlarmControllerTest {
    private final static String APPLICATION_ID = "test-application";
    private final static String APPLICATION_ID_UPDATED = "test-application-tomcat";
   
    private final static String SERVICE_TYPE = "tomcat";
    
    private final static String USER_GROUP_ID = "test-pinpoint_group";
    private final static String USER_GROUP_ID_UPDATED = "test-pinpoint_team";
    
    private final static String CHECKER_NAME = "ERROR_COUNT";
    private final static String CHECKER_NAME_UPDATED = "SLOW_COUNT";
    
    private final static int THRESHOLD = 100;
    private final static int THRESHOLD_UPDATED = 10;
    
    private final static boolean  SMS_SEND = false;
    private final static boolean  SMS_SEND_UPDATED = true;
    
    private final static boolean  EMAIL_SEND = true;
    private final static boolean  EMAIL_SEND_UPDATED = false;

    private final static String NOTES = "for unit test";
    private final static String NOTES_UPDATED = "";
    
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private AlarmDao alarmDao;

    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.alarmDao.deleteRuleByUserGroupId(USER_GROUP_ID);
        this.alarmDao.deleteRuleByUserGroupId(USER_GROUP_ID_UPDATED);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void insertAndSelectAndDeleteRule() throws Exception {
        String jsonParm = "{" +
                            "\"applicationId\" : \"" + APPLICATION_ID + "\"," + 
                            "\"serviceType\" : \"" + SERVICE_TYPE + "\"," + 
                            "\"userGroupId\" : \"" + USER_GROUP_ID + "\"," + 
                            "\"checkerName\" : \"" + CHECKER_NAME + "\"," + 
                            "\"threshold\" : " + THRESHOLD + "," + 
                            "\"smsSend\" : " + SMS_SEND + "," + 
                            "\"emailSend\" : \"" + EMAIL_SEND  + "\"," + 
                            "\"notes\" : \"" + NOTES + "\"" + 
                          "}"; 
                           
        MvcResult result = this.mockMvc.perform(post("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParm))
                                            .andExpect(status().isOk())
                                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                                            .andReturn();
        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> resultMap = objectMapper.readValue(content, HashMap.class);
        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        Assert.assertNotNull(resultMap.get("ruleId"));
        
        this.mockMvc.perform(get("/alarmRule.pinpoint?userGroupId=" + USER_GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0]", hasKey("applicationId")))
                .andExpect(jsonPath("$[0]", hasKey("serviceType")))
                .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
                .andExpect(jsonPath("$[0]", hasKey("checkerName")))
                .andExpect(jsonPath("$[0]", hasKey("threshold")))
                .andExpect(jsonPath("$[0]", hasKey("smsSend")))
                .andExpect(jsonPath("$[0]", hasKey("emailSend")))
                .andExpect(jsonPath("$[0]", hasKey("notes")))
                .andReturn();
        
        this.mockMvc.perform(delete("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"ruleId\" : \"" + resultMap.get("ruleId") + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void updateRule() throws Exception {
        String jsonParm = "{" +
                            "\"applicationId\" : \"" + APPLICATION_ID + "\"," + 
                            "\"serviceType\" : \"" + SERVICE_TYPE + "\"," + 
                            "\"userGroupId\" : \"" + USER_GROUP_ID + "\"," + 
                            "\"checkerName\" : \"" + CHECKER_NAME + "\"," + 
                            "\"threshold\" : " + THRESHOLD + "," + 
                            "\"smsSend\" : " + SMS_SEND + "," + 
                            "\"emailSend\" : \"" + EMAIL_SEND  + "\"," + 
                            "\"notes\" : \"" + NOTES + "\"" + 
                          "}"; 
                           
        MvcResult result = this.mockMvc.perform(post("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParm))
                                            .andExpect(status().isOk())
                                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                                            .andReturn();
        String content = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> resultMap = objectMapper.readValue(content, HashMap.class);
        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        Assert.assertNotNull(resultMap.get("ruleId"));
        
        String updatedJsonParm = "{" +
                "\"ruleId\" : \"" + resultMap.get("ruleId") + "\"," + 
                "\"applicationId\" : \"" + APPLICATION_ID_UPDATED + "\"," + 
                "\"serviceType\" : \"" + SERVICE_TYPE + "\"," + 
                "\"userGroupId\" : \"" + USER_GROUP_ID_UPDATED + "\"," + 
                "\"checkerName\" : \"" + CHECKER_NAME_UPDATED + "\"," + 
                "\"threshold\" : " + THRESHOLD_UPDATED + "," + 
                "\"smsSend\" : " + SMS_SEND_UPDATED + "," + 
                "\"emailSend\" : \"" + EMAIL_SEND_UPDATED  + "\"," + 
                "\"notes\" : \"" + NOTES_UPDATED + "\"" + 
              "}"; 
        
        this.mockMvc.perform(put("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content(updatedJsonParm))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andReturn();
        
        this.mockMvc.perform(delete("/alarmRule.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"ruleId\" : \"" + resultMap.get("ruleId") + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
    
    @Test
    public void checkerTest() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/alarmRule/checker.pinpoint").contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json;charset=UTF-8"))
                                .andExpect(jsonPath("$").isArray())
                                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> checkerList = objectMapper.readValue(content, List.class);
        Assert.assertNotEquals(checkerList.size(), 0);
    }
}
