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

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Taejin Koo
 */
public final class ControllerUtils {

    public static ModelAndView createJsonView() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("jsonView");
        
        return mv;
    }
    
    public static ModelAndView createJsonView(boolean success) {
        return createJsonView(success, null);
    }
    
    public static ModelAndView createJsonView(boolean success, Object message) {
        ModelAndView mv = createJsonView();

        if (success) {
            mv.addObject("result", "success");
        } else {
            mv.addObject("result", "fail");
        }

        if (message != null) {
            mv.addObject("message", message);
        }

        return mv;
    }

}
