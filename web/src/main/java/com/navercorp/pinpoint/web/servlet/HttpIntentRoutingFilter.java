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
import java.util.StringTokenizer;

/**
 * @author youngjin.kim2
 */
public class HttpIntentRoutingFilter implements Filter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String legacyEntryHtmlResource;
    private final String v2EntryHtmlResource;
    private final String v3EntryHtmlResource;

    public HttpIntentRoutingFilter(
            String legacyEntryHtmlResource,
            String v2EntryHtmlResource,
            String v3EntryHtmlResource
    ) {
        this.legacyEntryHtmlResource = legacyEntryHtmlResource;
        this.v2EntryHtmlResource = v2EntryHtmlResource;
        this.v3EntryHtmlResource = v3EntryHtmlResource;
    }

    public HttpIntentRoutingFilter(String htmlResource) {
        this(htmlResource, htmlResource, htmlResource);
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
        RequestType type = getRequestType(request);
        switch (type) {
            case LEGACY_FRONT_ENTRY:
                forwardToFrontAppEntry(request, response, chain, this.legacyEntryHtmlResource);
                break;
            case V2_FRONT_ENTRY:
                forwardToFrontAppEntry(request, response, chain, this.v2EntryHtmlResource);
                break;
            case V3_FRONT_ENTRY:
                forwardToFrontAppEntry(request, response, chain, this.v3EntryHtmlResource);
                break;
            case LEGACY_STATIC_RESOURCE:
            case V2_STATIC_RESOURCE:
            case V3_STATIC_RESOURCE:
            case REST_API:
            case UNKNOWN:
                chain.doFilter(request, response);
        }
    }

    private void forwardToFrontAppEntry(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain,
            String htmlResource
    ) throws ServletException, IOException {
        if (htmlResource == null) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        logger.debug("requestUri: {} --(forward)--> {}", httpRequest.getRequestURI(), htmlResource);

        RequestDispatcher dispatcher = request.getRequestDispatcher(htmlResource);
        dispatcher.forward(request, response);
    }

    private static RequestType getRequestType(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            return RequestType.UNKNOWN;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        StringTokenizer tokenizer = new StringTokenizer(httpRequest.getRequestURI(), "/");
        if (!tokenizer.hasMoreTokens()) {
            return RequestType.LEGACY_FRONT_ENTRY;
        }

        String t0 = tokenizer.nextToken();
        switch (t0) {
            case "api":
                return RequestType.REST_API;
            case "v2":
                return getRequestType(tokenizer, RequestType.V2_STATIC_RESOURCE, RequestType.V2_FRONT_ENTRY);
            case "v3":
                return getRequestType(tokenizer, RequestType.V3_STATIC_RESOURCE, RequestType.V3_FRONT_ENTRY);
            default:
                return getRequestType(t0, RequestType.LEGACY_STATIC_RESOURCE, RequestType.LEGACY_FRONT_ENTRY);
        }
    }

    private static RequestType getRequestType(
            StringTokenizer tokenizer,
            RequestType resourceType,
            RequestType entryType
    ) {
        if (tokenizer.hasMoreElements()) {
            return getRequestType(tokenizer.nextToken(), resourceType, entryType);
        } else {
            return entryType;
        }
    }

    private static RequestType getRequestType(String token, RequestType resourceType, RequestType entryType) {
        if (token.equals("assets") || token.indexOf('.') >= 0) {
            return resourceType;
        } else {
            return entryType;
        }
    }

    private enum RequestType {
        LEGACY_FRONT_ENTRY,
        LEGACY_STATIC_RESOURCE,
        V2_FRONT_ENTRY,
        V2_STATIC_RESOURCE,
        V3_FRONT_ENTRY,
        V3_STATIC_RESOURCE,
        REST_API,
        UNKNOWN,
    }

}
