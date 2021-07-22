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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class PreAuthenticationCheckFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            chain.doFilter(request, response);
            return;
        }

        if (authentication.isAuthenticated() && ((HttpServletRequest) request).getRequestURI().equals(BasicLoginConstants.URI_LOGIN)) {
            ((HttpServletResponse) response).sendRedirect(BasicLoginConstants.URI_MAIN);
        } else {
            chain.doFilter(request, response);
        }
    }

}