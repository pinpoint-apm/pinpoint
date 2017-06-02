/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.ApplicationAgentHostList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Taejin Koo
 */

@Controller
public class ApplicationController {

    @Autowired
    private AgentInfoService agentInfoService;

    @RequestMapping(value = "/getApplicationHostInfo", method = RequestMethod.GET)
    @ResponseBody
    public ApplicationAgentHostList getApplicationHostInfo (
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) throws Exception {
        return agentInfoService.getApplicationAgentHostList(offset, limit);
    }

}