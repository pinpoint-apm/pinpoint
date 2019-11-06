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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;

/**
 * @author minwoo.jung
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:servlet-context.xml", "classpath:applicationContext-web.xml"})
public class UserControllerTest {
    
    private final static String USER_ID = "naver00";
    private final static String USER_NAME = "minwoo";
    private final static String USER_NAME_UPDATED = "minwoo.jung";
    private final static String USER_DEPARTMENT = "Web platfrom development team";
    private final static String USER_PHONENUMBER = "01012347890";
    private final static String USER_PHONENUMBER_UPDATED = "01000000000";
    private final static String USER_EMAIL = "min@naver.com";
    private final static String USER_EMAIL_UPDATED = "minwoo@naver.com";
    
    @Autowired
    private WebApplicationContext wac;
 
    
    @Autowired
    private UserDao userDao;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        userDao.deleteUser(USER_ID);
    }
    
    @Test
    public void insertAndSelectAndDeleteUser() throws Exception {
        String jsonParm = "{" +
                            "\"userId\" : \"" + USER_ID + "\"," + 
                            "\"name\" : \"" + USER_NAME + "\"," + 
                            "\"department\" : \"" + USER_DEPARTMENT + "\"," + 
                            "\"phoneNumber\" : \"" + USER_PHONENUMBER + "\"," + 
                            "\"email\" : \"" + USER_EMAIL + "\"" + 
                          "}"; 
                           
        this.mockMvc.perform(post("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParm))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
        
        this.mockMvc.perform(get("/user.pinpoint").contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$[0]", hasKey("userId")))
                        .andExpect(jsonPath("$[0]", hasKey("name")))
                        .andExpect(jsonPath("$[0]", hasKey("department")))
                        .andExpect(jsonPath("$[0]", hasKey("phoneNumber")))
                        .andExpect(jsonPath("$[0]", hasKey("email")))
                        .andReturn();
        
        this.mockMvc.perform(get("/user.pinpoint?searchKey=" + USER_NAME).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$[0]", hasKey("userId")))
        .andExpect(jsonPath("$[0]", hasKey("name")))
        .andExpect(jsonPath("$[0]", hasKey("department")))
        .andExpect(jsonPath("$[0]", hasKey("phoneNumber")))
        .andExpect(jsonPath("$[0]", hasKey("email")))
        .andReturn();

        this.mockMvc.perform(get("/user.pinpoint?userId=" + USER_ID).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$[0]", hasKey("userId")))
        .andExpect(jsonPath("$[0]", hasKey("name")))
        .andExpect(jsonPath("$[0]", hasKey("department")))
        .andExpect(jsonPath("$[0]", hasKey("phoneNumber")))
        .andExpect(jsonPath("$[0]", hasKey("email")))
        .andReturn();
        
        this.mockMvc.perform(get("/user.pinpoint?searchKey=" + USER_DEPARTMENT).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$[0]", hasKey("userId")))
        .andExpect(jsonPath("$[0]", hasKey("name")))
        .andExpect(jsonPath("$[0]", hasKey("department")))
        .andExpect(jsonPath("$[0]", hasKey("phoneNumber")))
        .andExpect(jsonPath("$[0]", hasKey("email")))
        .andReturn();
        
        this.mockMvc.perform(delete("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + USER_ID + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
    
    @Test
    public void selectUser() throws Exception {
        String jsonParm = "{" +
                            "\"userId\" : \"" + USER_ID + "\"," + 
                            "\"name\" : \"" + USER_NAME + "\"," + 
                            "\"department\" : \"" + USER_DEPARTMENT + "\"," + 
                            "\"phoneNumber\" : \"" + USER_PHONENUMBER + "\"," + 
                            "\"email\" : \"" + USER_EMAIL + "\"" + 
                          "}"; 
                           
        this.mockMvc.perform(post("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParm))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
        
        this.mockMvc.perform(get("/user.pinpoint?userId=" + USER_ID).contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$[0]", hasKey("userId")))
                        .andExpect(jsonPath("$[0].userId").value(USER_ID))
                        .andExpect(jsonPath("$[0]", hasKey("name")))
                        .andExpect(jsonPath("$[0]", hasKey("department")))
                        .andExpect(jsonPath("$[0]", hasKey("phoneNumber")))
                        .andExpect(jsonPath("$[0]", hasKey("email")))
                        .andReturn();
        
        
        this.mockMvc.perform(delete("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + USER_ID + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
    
    @Test
    public void updateUser() throws Exception {
        String jsonParamforUserInfo = "{" +
                            "\"userId\" : \"" + USER_ID + "\"," + 
                            "\"name\" : \"" + USER_NAME + "\"," + 
                            "\"department\" : \"" + USER_DEPARTMENT + "\"," + 
                            "\"phoneNumber\" : \"" + USER_PHONENUMBER + "\"," + 
                            "\"email\" : \"" + USER_EMAIL + "\"" + 
                          "}"; 
                           
        this.mockMvc.perform(post("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParamforUserInfo))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
        
        String jsonParmForUserInfoUpdated = "{" +
                "\"userId\" : \"" + USER_ID + "\"," + 
                "\"name\" : \"" + USER_NAME_UPDATED + "\"," + 
                "\"department\" : \"" + USER_DEPARTMENT + "\"," + 
                "\"phoneNumber\" : \"" + USER_PHONENUMBER_UPDATED + "\"," + 
                "\"email\" : \"" + USER_EMAIL_UPDATED + "\"" + 
              "}"; 
        
        this.mockMvc.perform(put("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content(jsonParmForUserInfoUpdated))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
        
        this.mockMvc.perform(delete("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + USER_ID + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
}
