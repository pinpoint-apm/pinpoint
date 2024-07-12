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

import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.vo.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import static com.navercorp.pinpoint.web.TestTraceUtils.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class ConfigControllerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private WebApplicationContext wac;
    
    @Autowired
    private UserDao userDao;
    
    private MockMvc mockMvc;
    
    private static final  User user = new User("naver01", "min", "pinpoint", 82, "010", "min@naver0.com");

    @BeforeEach
    public void before() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.userDao.insertUser(user);
    }
    
    @AfterEach
    public void after() {
        this.userDao.deleteUser(user.getUserId());
    }
    

    @Test
    public void configuration() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/configuration").contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType("application/json;charset=UTF-8"))
                                        .andExpect(jsonPath("$", hasKey("showActiveThread")))
                                        .andExpect(jsonPath("$", hasKey("editUserInfo")))
                                        .andExpect(jsonPath("$", hasKey("sendUsage")))
                                        .andExpect(jsonPath("$", hasKey("userId")))
                                        .andExpect(jsonPath("$", hasKey("userName")))
                                        .andExpect(jsonPath("$", hasKey("userDepartment")))
                                        .andReturn();
        String content = result.getResponse().getContentAsString();
        logger.debug(content);
        
        result = this.mockMvc.perform(get("/api/configuration").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", hasKey("showActiveThread")))
                .andExpect(jsonPath("$", hasKey("editUserInfo")))
                .andExpect(jsonPath("$", hasKey("sendUsage")))
                .andReturn();
        content = result.getResponse().getContentAsString();
        logger.debug(content);
    }

}
