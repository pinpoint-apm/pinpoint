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

import org.junit.After;
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
import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
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
    private final static String TEST_USER_GROUP_ID_UPDATED = "testUserGroupUpdated";

    private final static String TEST_USER_GROUP_ID2 = "testUserGroup2";
    private final static String TEST_USER_GROUP_ID3 = "testUserGroup3";
    
    private final static String TEST_USER_GROUP_MEMBER_ID = "naver01";
    private final static String TEST_USER_GROUP_MEMBER_ID2 = "naver02";
    private final static String TEST_USER_GROUP_MEMBER_ID_UPDATE = "naver010";
    
    
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private UserGroupDao userGroupDao;
    
    @Autowired
    private UserDao userDao;
    
    private MockMvc mockMvc;
    
    private User user = new User(TEST_USER_GROUP_MEMBER_ID, "userName", "pinpoint_team", "0101234", "pinpoint_team@navercorp.com");
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        userGroupDao.deleteUserGroup(new UserGroup("", TEST_USER_GROUP_ID));
        userGroupDao.deleteUserGroup(new UserGroup("", TEST_USER_GROUP_ID_UPDATED));
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID, TEST_USER_GROUP_MEMBER_ID));
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID, TEST_USER_GROUP_MEMBER_ID_UPDATE));
        
        userDao.insertUser(user);

        //for selectUserGroupByUserId test
        userGroupDao.createUserGroup(new UserGroup("", TEST_USER_GROUP_ID2));
        userGroupDao.createUserGroup(new UserGroup("", TEST_USER_GROUP_ID3));
        userGroupDao.insertMember(new UserGroupMember(TEST_USER_GROUP_ID2, TEST_USER_GROUP_MEMBER_ID2));
        userGroupDao.insertMember(new UserGroupMember(TEST_USER_GROUP_ID3, TEST_USER_GROUP_MEMBER_ID2));
    }
    
    @After
    public void after(){
        userDao.deleteUser(user);
        
        //for selectUserGroupByUserId test
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID2, TEST_USER_GROUP_MEMBER_ID2));
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID3, TEST_USER_GROUP_MEMBER_ID2));
        userGroupDao.deleteUserGroup(new UserGroup("", TEST_USER_GROUP_ID2));
        userGroupDao.deleteUserGroup(new UserGroup("", TEST_USER_GROUP_ID3));
    }
    
    @Test
    public void selectUserGroupByUserId() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/userGroup.pinpoint?userId=" + TEST_USER_GROUP_MEMBER_ID2).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map> userGroupList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(userGroupList.size(), 2);
    }

    @Test
    public void selectUserGroupByUserGroupId() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/userGroup.pinpoint?userGroupId=" + TEST_USER_GROUP_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map> userGroupList = objectMapper.readValue(content, List.class);
        Assert.assertEquals(userGroupList.size(), 2);
    }

    @Test
    public void createAndSelectAndDeleteUserGroup() throws Exception {
        this.mockMvc.perform(post("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("number")))
                            .andReturn();
        
        this.mockMvc.perform(get("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$[0]", hasKey("id")))
                            .andReturn();

        this.mockMvc.perform(delete("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("result")))
                            .andExpect(jsonPath("$.result").value("SUCCESS"))
                            .andReturn();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void updateUserGroup() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("number")))
                            .andReturn();
        
        String content = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> resultMap = objectMapper.readValue(content, HashMap.class);
        String userGroupNumber = resultMap.get("number");
        
        this.mockMvc.perform(put("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"number\" : \"" + userGroupNumber + "\", \"id\" : \"" + TEST_USER_GROUP_ID_UPDATED + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();


        this.mockMvc.perform(delete("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID_UPDATED + "\"}"))
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
    public void deleteUserGroupError() throws Exception {
        this.mockMvc.perform(delete("/userGroup.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("errorCode")))
                        .andExpect(jsonPath("$.errorCode").value("500"))
                        .andReturn();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void insertAndSelectDeleteMember() throws Exception {
            this.mockMvc.perform(post("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("result")))
                            .andExpect(jsonPath("$.result").value("SUCCESS"))
                            .andReturn();
    
            MvcResult andReturn = this.mockMvc.perform(get("/userGroup/member.pinpoint?userGroupId=" + TEST_USER_GROUP_ID))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
                            .andExpect(jsonPath("$[0]", hasKey("memberId")))
                            .andReturn();

            
            this.mockMvc.perform(delete("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType("application/json;charset=UTF-8"))
                            .andExpect(jsonPath("$", hasKey("result")))
                            .andExpect(jsonPath("$.result").value("SUCCESS"))
                            .andReturn();
    }
    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void updateMember() throws Exception  {
//        MvcResult mvcResult = this.mockMvc.perform(post("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
//                        .andExpect(jsonPath("$", hasKey("number")))
//                        .andReturn();
//        
//        String content = mvcResult.getResponse().getContentAsString();
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, String> resultMap = objectMapper.readValue(content, HashMap.class);
//        String userGroupMemberNumber = resultMap.get("number");
//        
//        this.mockMvc.perform(put("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"number\" : \"" + userGroupMemberNumber + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID_UPDATE + "\"}"))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
//                        .andExpect(jsonPath("$", hasKey("result")))
//                        .andExpect(jsonPath("$.result").value("SUCCESS"))
//                        .andReturn();
//        
//        this.mockMvc.perform(delete("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID_UPDATE + "\"}"))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
//                        .andExpect(jsonPath("$", hasKey("result")))
//                        .andExpect(jsonPath("$.result").value("SUCCESS"))
//                        .andReturn();
//    }
    
    @Test
    public void deleteUserGroupMemberError() throws Exception {
        this.mockMvc.perform(delete("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("errorCode")))
                        .andExpect(jsonPath("$.errorCode").value("500"))
                        .andReturn();
    }
    
    @Test
    public void insertUserGroupMemberError() throws Exception {
        this.mockMvc.perform(post("/userGroup/member.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("errorCode")))
                        .andExpect(jsonPath("$.errorCode").value("500"))
                        .andReturn();
    }
    
    
}
