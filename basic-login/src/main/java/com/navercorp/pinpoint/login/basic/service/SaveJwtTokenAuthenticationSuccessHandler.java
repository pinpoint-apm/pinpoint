/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.login.basic.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SaveJwtTokenAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final BasicLoginService basicLoginService;

    public SaveJwtTokenAuthenticationSuccessHandler(BasicLoginService basicLoginService) {
        this.basicLoginService = Objects.requireNonNull(basicLoginService, "basicLoginService");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.debug("onAuthenticationSuccess() started");

        String userId = authentication.getName();

        Cookie newCookie = basicLoginService.createNewCookie(userId);
        response.addCookie(newCookie);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.sendRedirect(BasicLoginConstants.URI_MAIN);
    }

}