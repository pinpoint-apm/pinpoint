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

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class HttpIntentRoutingFilter extends HttpFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final VersionPrefixRewriter rewriter;

    public HttpIntentRoutingFilter(VersionPrefixRewriter rewriter) {
        this.rewriter = Objects.requireNonNull(rewriter, "rewriter");
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
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
