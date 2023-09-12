/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.web.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class HttpIntentRoutingFilter implements Filter {

    private static final FrontApplicationRegistration DEFAULT_FRONT_REGISTRATION = FrontApplicationRegistration.of("/");

    private static final List<String> RESOURCE_BASE_PATHS = List.of(
            "assets",
            "fonts",
            "img"
    );

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<FrontApplicationRegistration> registrations;

    public HttpIntentRoutingFilter(List<FrontApplicationRegistration> registrations) {
        this.registrations = Objects.requireNonNullElse(registrations, List.of(DEFAULT_FRONT_REGISTRATION));
    }

    public HttpIntentRoutingFilter() {
        this(List.of(DEFAULT_FRONT_REGISTRATION));
    }

    @Override
    public void init(FilterConfig config) {
        logger.info("Initialized rewriter v2, name: {}", config.getFilterName());
    }

    @Override
    public void destroy() {
        logger.info("Destroyed rewriter v2");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            route((HttpServletRequest) request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void route(
            HttpServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        for (FrontApplicationRegistration registration: this.registrations) {
            if (uri.startsWith(registration.getBasePath())) {
                routeToFrontApplication(request, response, chain, registration);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void routeToFrontApplication(
            HttpServletRequest request,
            ServletResponse response,
            FilterChain chain,
            FrontApplicationRegistration frontRegistration
    ) throws ServletException, IOException {
        String basePath = frontRegistration.getBasePath();
        String path = trimFirstSlash(request.getRequestURI().substring(basePath.length()));
        String firstToken = path.split("/", 2)[0];
        if (isStaticResource(firstToken)) {
            chain.doFilter(request, response);
        } else {
            String entryHtml = basePath + "/index.html";
            forwardTo(request, response, entryHtml);
            logger.debug("requestUri: {} --(forward)--> {}", request.getRequestURI(), entryHtml);
        }
    }

    private static void forwardTo(
            HttpServletRequest request,
            ServletResponse response,
            String entryHtml
    ) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(entryHtml);
        dispatcher.forward(request, response);
    }

    private static boolean isStaticResource(String firstToken) {
        for (String resourceBasePath : RESOURCE_BASE_PATHS) {
            if (firstToken.equals(resourceBasePath)) {
                return true;
            }
        }
        return firstToken.indexOf('.') != -1;
    }

    private static String trimFirstSlash(String s) {
        if (s.length() > 0 && s.charAt(0) == '/') {
            return s.substring(1);
        }
        return s;
    }

    public static class FrontApplicationRegistration {

        private final String basePath;

        public FrontApplicationRegistration(String basePath) {
            this.basePath = Objects.requireNonNull(basePath, "basePath");
        }

        public static FrontApplicationRegistration of(String basePath) {
            return new FrontApplicationRegistration(basePath);
        }

        public String getBasePath() {
            return basePath;
        }
    }

}
