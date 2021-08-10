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

package com.navercorp.pinpoint.web.security.login;

import com.navercorp.pinpoint.web.config.BasicLoginConfig;

import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Service
@Profile("basicLogin")
public class BasicLoginService {

    private final PinpointMemoryUserDetailsService pinpointMemoryUserDetailsService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JwtService jwtService;

    public BasicLoginService(BasicLoginConfig basicLoginConfig) {
        PinpointMemoryUserDetailsService pinpointMemoryUserDetailsService = new PinpointMemoryUserDetailsService(basicLoginConfig);
        this.pinpointMemoryUserDetailsService = pinpointMemoryUserDetailsService;

        this.jwtService = new JwtService(basicLoginConfig);
    }

    public UserDetails getUserDetails(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            String name = cookie.getName();

            if (BasicLoginConstants.PINPOINT_JWT_COOKIE_NAME.equals(name)) {
                String pinpointJwtToken = cookie.getValue();

                try {
                    Date expirationDate = jwtService.getExpirationDate(pinpointJwtToken);
                    if (expirationDate.getTime() > System.currentTimeMillis()) {
                        String userId = jwtService.getUserId(pinpointJwtToken);

                        UserDetails userDetails = pinpointMemoryUserDetailsService.loadUserByUsername(String.valueOf(userId));
                        if (userDetails != null) {
                            return userDetails;
                        }
                    } else {
                        logger.warn("This token already expired.");
                    }
                } catch (ExpiredJwtException e) {
                    logger.warn("This token already expired. message:{}", e.getMessage(), e);
                }
            }
        }

        return null;
    }

    public Cookie createNewCookie(String userId) {
        UserDetails userDetails = pinpointMemoryUserDetailsService.loadUserByUsername(userId);
        if (userDetails == null) {
            throw new IllegalArgumentException("Could not load User information.");
        }

        String token = jwtService.createToken(userDetails);
        Cookie cookie = new Cookie(BasicLoginConstants.PINPOINT_JWT_COOKIE_NAME, token);
        cookie.setPath("/");

        long maxAge = TimeUnit.MILLISECONDS.toSeconds(jwtService.getExpirationTimeMillis());
        cookie.setMaxAge((int) maxAge);
        return cookie;
    }

    public UserDetailsService getUserDetailsService() {
        return pinpointMemoryUserDetailsService;
    }

}