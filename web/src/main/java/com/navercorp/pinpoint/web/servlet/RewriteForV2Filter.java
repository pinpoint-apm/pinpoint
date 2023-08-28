/*
 * Copyright 2018 NAVER Corp.
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

/**
 * @author Taejin Koo
 */
public class RewriteForV2Filter implements Filter {

    public static final String DEFAULT_INDEX = "/index.html";
    private static final char PATH_DELIMITER = '/';
    private static final String[] URI_PREFIXES_FOR_FRONT = {
            "/auth",
            "/browserNotSupported",
            "/config",
            "/error",
            "/filteredMap",
            "/inspector",
            "/main",
            "/realtime",
            "/scatterFullScreenMode",
            "/threadDump",
            "/transactionDetail",
            "/transactionList",
            "/transactionView",
            "/urlStatistic",
            "/metric",
    };

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebugEnabled = logger.isDebugEnabled();

    private final boolean enabled;

    public RewriteForV2Filter(boolean enabled) {
        this.enabled = enabled;
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
        if (this.shouldServeFrontApplication(request)) {
            this.serveFrontApplication(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void serveFrontApplication(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (isDebugEnabled) {
            logger.debug("requestUri: {} --(forward)--> {}", ((HttpServletRequest) req).getRequestURI(), DEFAULT_INDEX);
        }
        RequestDispatcher dispatcher = req.getRequestDispatcher(DEFAULT_INDEX);
        dispatcher.forward(req, res);
    }

    private boolean shouldServeFrontApplication(ServletRequest req) {
        return this.enabled && isURIForFront(getRequestURI(req));
    }

    private static boolean isURIForFront(String uri) {
        if (uri == null) {
            return false;
        }

        for (String prefix : URI_PREFIXES_FOR_FRONT) {
            if (uri.equals(prefix) || uri.startsWith(prefix + PATH_DELIMITER)) {
                return true;
            }
        }
        return false;
    }

    private static String getRequestURI(ServletRequest req) {
        if (req instanceof HttpServletRequest) {
            return ((HttpServletRequest) req).getRequestURI();
        }
        return null;
    }

}
