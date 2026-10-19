/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAuthInterceptorTest {

    @Test
    void emptyPasswordShouldRejectRequest() {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor("");
        MockHttpServletRequest request = request(null);

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void nullPasswordShouldRejectRequest() {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor(null);
        MockHttpServletRequest request = request("");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void missingPasswordShouldRejectRequest() {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor("secret");

        assertThatThrownBy(() -> interceptor.preHandle(request(null), new MockHttpServletResponse(), null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void invalidPasswordShouldRejectRequest() {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor("secret");

        assertThatThrownBy(() -> interceptor.preHandle(request("wrong"), new MockHttpServletResponse(), null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void validPasswordShouldPass() throws Exception {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor("secret");

        assertThat(interceptor.preHandle(request("secret"), new MockHttpServletResponse(), null)).isTrue();
    }

    private static MockHttpServletRequest request(String password) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/agentIdMap");
        if (password != null) {
            request.setParameter("password", password);
        }
        return request;
    }
}
