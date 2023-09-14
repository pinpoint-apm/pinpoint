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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author youngjin.kim2
 */
public class HttpIntentRoutingFilter extends HttpFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final VersionPrefixRewriter rewriter = new VersionPrefixRewriter();


    public HttpIntentRoutingFilter() {
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

        final String rewriteUrl = rewriter.rewrite(uri);
        if (rewriteUrl == null) {
            chain.doFilter(request, response);
            return;
        }
        logger.debug("requestUri: {} --(forward)--> {}", uri, rewriteUrl);
        RequestDispatcher dispatcher = request.getRequestDispatcher(rewriteUrl);
        dispatcher.forward(request, response);
    }

}
