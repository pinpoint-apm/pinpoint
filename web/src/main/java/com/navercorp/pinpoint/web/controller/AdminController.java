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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.AdminService;

/**
 * @author netspider
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "/removeApplicationName", method = RequestMethod.GET)
    @ResponseBody
    public String removeApplicationName(@RequestParam("applicationName") String applicationName) {
        logger.info("remove application name. {}", applicationName);
        try {
            adminService.removeApplicationName(applicationName);
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing applicationName", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/removeAgentId", method = RequestMethod.GET)
    @ResponseBody
    public String removeAgentId(
            @RequestParam(value = "applicationName", required = true) String applicationName,
            @RequestParam(value = "agentId", required = true) String agentId) {
        logger.info("remove agent id - ApplicationName: [{}], Agent ID: [{}]", applicationName, agentId);
        try {
            adminService.removeAgentId(applicationName, agentId);
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing agentId", e);
            return e.getMessage();
        }
    }
}