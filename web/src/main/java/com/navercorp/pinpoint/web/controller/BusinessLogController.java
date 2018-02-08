/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.service.BusinessLogService;
/**
 * [XINGUANG]Created by Administrator on 2017/6/12.
 */
@Controller
public class BusinessLogController {

    @Autowired
    private BusinessLogService businessLogService;
    @RequestMapping(value = "/getBusinessLog")
    @ResponseBody
    public String GetBusinessLog(@RequestParam(value= "agentId",required=true) String agentId,
                   @RequestParam (value= "transactionId", required=true) String transactionId,
                   @RequestParam(value= "spanId" , required=false) String spanId,
                   @RequestParam(value="time" , required=true) long time ) {

        // you should implement the logic to retrieve your agent’s logs.
        String str = businessLogService.getBusinessLog(agentId,transactionId,spanId,time);
        return str;
    }
}
