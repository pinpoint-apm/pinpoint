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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;
import com.navercorp.pinpoint.web.vo.UserGroupMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.web.TestTraceUtils.hasKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author minwoo.jung
 */
@Disabled
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext-web.xml"})
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

    private final ObjectMapper mapper = Jackson.newMapper();

    private MockMvc mockMvc;

    private final User user = new User(TEST_USER_GROUP_MEMBER_ID, "userName", "pinpoint_team", 82, "0101234", "pinpoint_team@navercorp.com");

    @BeforeEach
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

    @AfterEach
    public void after() {
        userDao.deleteUser(user.getUserId());

        //for selectUserGroupByUserId test
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID2, TEST_USER_GROUP_MEMBER_ID2));
        userGroupDao.deleteMember(new UserGroupMember(TEST_USER_GROUP_ID3, TEST_USER_GROUP_MEMBER_ID2));
        userGroupDao.deleteUserGroup(new UserGroup("", TEST_USER_GROUP_ID2));
        userGroupDao.deleteUserGroup(new UserGroup("", TEST_USER_GROUP_ID3));
    }

    @Test
    public void selectUserGroupByUserId() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/userGroup?userId=" + TEST_USER_GROUP_MEMBER_ID2).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<Map<String, Object>> userGroupList = mapper.readValue(content, TypeRef.listMap());
        assertThat(userGroupList).hasSize(2);
    }

    @Test
    public void selectUserGroupByUserGroupId() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/userGroup?userGroupId=" + TEST_USER_GROUP_ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<Map<String, Object>> userGroupList = mapper.readValue(content, TypeRef.listMap());
        assertThat(userGroupList).hasSize(2);
    }

    @Test
    public void createAndSelectAndDeleteUserGroup() throws Exception {
        this.mockMvc.perform(post("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("number")))
                .andReturn();

        this.mockMvc.perform(get("/api/userGroup").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0]", hasKey("id")))
                .andReturn();

        this.mockMvc.perform(delete("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andReturn();
    }

    @Test
    public void updateUserGroup() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("number")))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        Map<String, ?> resultMap = mapper.readValue(content, TypeRef.map());
        String userGroupNumber = (String) resultMap.get("number");

        this.mockMvc.perform(put("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{\"number\" : \"" + userGroupNumber + "\", \"id\" : \"" + TEST_USER_GROUP_ID_UPDATED + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andReturn();


        this.mockMvc.perform(delete("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{\"id\" : \"" + TEST_USER_GROUP_ID_UPDATED + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andReturn();
    }

    @Test
    public void createUserGroupError() throws Exception {
        this.mockMvc.perform(post("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("errorCode")))
                .andExpect(jsonPath("$.errorCode").value("500"))
                .andReturn();
    }

    @Test
    public void deleteUserGroupError() throws Exception {
        this.mockMvc.perform(delete("/api/userGroup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("errorCode")))
                .andExpect(jsonPath("$.errorCode").value("500"))
                .andReturn();
    }

    @Test
    public void insertAndSelectDeleteMember() throws Exception {
        this.mockMvc.perform(post("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andReturn();

        this.mockMvc.perform(get("/api/userGroup/member?userGroupId=" + TEST_USER_GROUP_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0]", hasKey("userGroupId")))
                .andExpect(jsonPath("$[0]", hasKey("memberId")))
                .andReturn();


        this.mockMvc.perform(delete("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("result")))
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andReturn();
    }

//    @SuppressWarnings("unchecked")
//    @Test
//    public void updateMember() throws Exception  {
//        MvcResult mvcResult = this.mockMvc.perform(post("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID + "\"}"))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
//                        .andExpect(jsonPath("$", hasKey("number")))
//                        .andReturn();
//        
//        String content = mvcResult.getResponse().getContentAsString();
//        Map<String, String> resultMap = mapper.readValue(content, HashMap.class);
//        String userGroupMemberNumber = resultMap.get("number");
//        
//        this.mockMvc.perform(put("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{\"number\" : \"" + userGroupMemberNumber + "\"," + "\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID_UPDATE + "\"}"))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
//                        .andExpect(jsonPath("$", hasKey("result")))
//                        .andExpect(jsonPath("$.result").value("SUCCESS"))
//                        .andReturn();
//        
//        this.mockMvc.perform(delete("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{\"userGroupId\" : \"" + TEST_USER_GROUP_ID + "\", \"memberId\" : \"" + TEST_USER_GROUP_MEMBER_ID_UPDATE + "\"}"))
//                        .andExpect(status().isOk())
//                        .andExpect(content().contentType("application/json;charset=UTF-8"))
//                        .andExpect(jsonPath("$", hasKey("result")))
//                        .andExpect(jsonPath("$.result").value("SUCCESS"))
//                        .andReturn();
//    }

    @Test
    public void deleteUserGroupMemberError() throws Exception {
        this.mockMvc.perform(delete("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("errorCode")))
                .andExpect(jsonPath("$.errorCode").value("500"))
                .andReturn();
    }

    @Test
    public void insertUserGroupMemberError() throws Exception {
        this.mockMvc.perform(post("/api/userGroup/member").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("errorCode")))
                .andExpect(jsonPath("$.errorCode").value("500"))
                .andReturn();
    }

}
