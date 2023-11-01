/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class HttpIntentRoutingFilterTest {

    public static final String ADMIN_REWRITE_TARGET = "/auth";

    @Test
    public void rewriteTest() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            HttpIntentRoutingFilter httpIntentRoutingFilter = newHttpFilter();

            HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
            Mockito.when(servletRequest.getRequestURI()).thenReturn(ADMIN_REWRITE_TARGET);

            HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
            FilterChain filterChain = Mockito.mock(FilterChain.class);

            httpIntentRoutingFilter.doFilter(servletRequest, servletResponse, filterChain);
        });
    }

    private HttpIntentRoutingFilter newHttpFilter() {
        VersionPrefixRewriter rewriter = new VersionPrefixRewriter();
        return new HttpIntentRoutingFilter(rewriter);
    }

    @Test
    public void restApiWithRewritePathTest() throws IOException, ServletException {
        HttpIntentRoutingFilter httpIntentRoutingFilter = newHttpFilter();

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getRequestURI()).thenReturn("/api" + ADMIN_REWRITE_TARGET + "/removeApplicationName");

        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        httpIntentRoutingFilter.doFilter(servletRequest, servletResponse, filterChain);

        Mockito.verify(filterChain).doFilter(Mockito.any(), Mockito.any());
    }

}
