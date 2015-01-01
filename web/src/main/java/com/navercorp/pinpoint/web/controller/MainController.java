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

import java.util.List;

import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.view.ApplicationGroup;
import com.navercorp.pinpoint.web.view.ServerTime;
import com.navercorp.pinpoint.web.vo.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author emeroad
 * @author netspider
 */
@Controller
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommonService commonService;

    @RequestMapping(value = "/applications", method = RequestMethod.GET)
    @ResponseBody
    public ApplicationGroup getApplicationGroup() {
        List<Application> applicationList = commonService.selectAllApplicationNames();
        logger.debug("/applications {}", applicationList);

        return new ApplicationGroup(applicationList);
    }

    @RequestMapping(value = "/serverTime", method = RequestMethod.GET)
    @ResponseBody
    public ServerTime getServerTime() {
        return new ServerTime();
    }
}
