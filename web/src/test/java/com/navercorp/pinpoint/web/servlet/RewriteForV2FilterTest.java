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

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class RewriteForV2FilterTest {

    public static final String ADMIN_REWRITE_TARGET = "/auth";

    @Test(expected = NullPointerException.class)
    public void rewriteTest() throws IOException, ServletException {
        RewriteForV2Filter rewriteForV2Filter = new RewriteForV2Filter(true);

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getRequestURI()).thenReturn(ADMIN_REWRITE_TARGET);

        ServletResponse servletResponse = Mockito.mock(ServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        rewriteForV2Filter.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Test
    public void restApiWithRewritePathTest() throws IOException, ServletException {
        RewriteForV2Filter rewriteForV2Filter = new RewriteForV2Filter(true);

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getRequestURI()).thenReturn(ADMIN_REWRITE_TARGET + "/removeApplicationName.pinpoint");

        ServletResponse servletResponse = Mockito.mock(ServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        rewriteForV2Filter.doFilter(servletRequest, servletResponse, filterChain);

        Mockito.verify(filterChain, Mockito.times(1)).doFilter(Mockito.any(), Mockito.any());
    }

}
