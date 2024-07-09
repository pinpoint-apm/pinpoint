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

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@RestController
@RequestMapping("/admin")
public class HandlerManagerController {

    private final HandlerManager handlerManager;

    public HandlerManagerController(HandlerManager handlerManager) {
        this.handlerManager = Objects.requireNonNull(handlerManager, "handlerManager");
    }

    @GetMapping(value = "/enableAccess")
    public ResponseEntity<SimpleResponse> enableAccess() {
        try {
            handlerManager.enableAccess();
            return ResponseEntity.ok(SimpleResponse.success());
        } catch (Exception e) {
            return unauthorizedResponse(e.getMessage());
        }
    }



    @GetMapping(value = "/disableAccess")
    public ResponseEntity<SimpleResponse> disableAccess() {
        try {
            handlerManager.disableAccess();
            return ResponseEntity.ok(SimpleResponse.success());
        } catch (Exception e) {
            return unauthorizedResponse(e.getMessage());
        }
    }

    @GetMapping(value = "/isEnable")
    public ResponseEntity<SimpleResponse> isEnable() {
        boolean isEnable = handlerManager.isEnable();

        SimpleResponse simpleResponse = SimpleResponse.success();
        simpleResponse.addAttribute("isEnable", isEnable);

        return ResponseEntity.ok(simpleResponse);
    }

    private ResponseEntity<SimpleResponse> unauthorizedResponse(String errorMessage) {
        SimpleResponse body = new SimpleResponse(false, errorMessage);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
