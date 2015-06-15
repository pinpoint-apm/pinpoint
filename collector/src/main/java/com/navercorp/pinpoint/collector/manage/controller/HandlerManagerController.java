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

package com.navercorp.pinpoint.collector.manage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.navercorp.pinpoint.collector.manage.HandlerManager;

/**
 * @author Taejin Koo
 */
@Controller
@RequestMapping("/admin")
public class HandlerManagerController {

    @Autowired
    private HandlerManager handlerManager;

    @RequestMapping(value = "/enableAccess", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView enableAccess() {
        try {
            handlerManager.enableAccess();
            return ControllerUtils.createJsonView(true);
        } catch (Exception e) {
            return ControllerUtils.createJsonView(false, e.getMessage());
        }
    }

    @RequestMapping(value = "/disableAccess", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView disableAccess() {
        try {
            handlerManager.disableAccess();
            return ControllerUtils.createJsonView(true);
        } catch (Exception e) {
            return ControllerUtils.createJsonView(false, e.getMessage());
        }
    }

    @RequestMapping(value = "/isEnable", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView isEnable() {
        boolean isEnable = handlerManager.isEnable();
        
        ModelAndView mv = ControllerUtils.createJsonView(true);
        mv.addObject("isEnable", isEnable);
        
        return mv;
    }

}
