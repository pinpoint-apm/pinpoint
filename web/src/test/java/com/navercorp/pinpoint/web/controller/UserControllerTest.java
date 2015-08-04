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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
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
    private final static String USER_DEPARTMENT = "Web platfrom development team";
    private final static String USER_PHONENUMBER = "01012347890";
    private final static String USER_EMAIL = "min@naver.com";
    
    @Autowired
    private WebApplicationContext wac;
 
    
    @Autowired
    private UserDao userDao;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        User user = new User();
        user.setUserId(USER_ID);
        userDao.deleteUser(user);
    }
    
    @Test
    public void insertAndDeleteUser() throws Exception {
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
        
        this.mockMvc.perform(delete("/user.pinpoint").contentType(MediaType.APPLICATION_JSON).content("{\"userId\" : \"" + USER_ID + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                        .andExpect(jsonPath("$", hasKey("result")))
                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                        .andReturn();
    }
}
