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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.UserGroupMember;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context.xml", "classpath:applicationContext-web.xml"})
public class UserGroupControllerTest {
    
    private final static String TEST_USER_GROUP_ID = "testUserGroup";
    private final static String TEST_USER_GROUP_MEMBER_ID = "naver01";
    
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private UserGroupDao userGroupDao;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        userGroupDao.deleteUserGroup(TEST_USER_GROUP_ID);
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID, TEST_USER_GROUP_MEMBER_ID));
    }

    @Test
    public void createAndDeleteUserGroup() throws Exception {
        this.mockMvc.perform(post("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("result")))
                            .andExpect(jsonPath("$.result").value("SUCCESS"))
                            .andReturn();

        this.mockMvc.perform(delete("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("result")))
                            .andExpect(jsonPath("$.result").value("SUCCESS"))
                            .andReturn();
    }
    
    @Test
    public void createUserGroupError() throws Exception {
        this.mockMvc.perform(post("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("errorCode")))
                        .andExpect(jsonPath("$.errorCode").value("500"))
                        .andReturn();
    }
    
    @Test
    public void createUserGroup2Error() throws Exception {
        this.mockMvc.perform(delete("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("errorCode")))
                        .andExpect(jsonPath("$.errorCode").value("500"))
                        .andReturn();
    }
    
    @Test
    public void insertAndDeleteMember() throws Exception  {
        this.mockMvc.perform(post("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"userGroupMemberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
        
        this.mockMvc.perform(delete("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"userGroupMemberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
    
    
    
}
