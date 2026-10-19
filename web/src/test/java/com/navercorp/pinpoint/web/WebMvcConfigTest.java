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

package com.navercorp.pinpoint.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebMvcConfigTest {

    @Test
    void registerAdminAuthInterceptorByDefault() {
        assertThat(interceptors(true)).hasSize(1);
    }

    @Test
    void skipAdminAuthInterceptorWhenPasswordDisabled() {
        assertThat(interceptors(false)).isEmpty();
    }

    private List<?> interceptors(boolean passwordEnabled) {
        WebMvcConfig config = new WebMvcConfig();
        ReflectionTestUtils.setField(config, "passwordEnabled", passwordEnabled);
        InterceptorRegistry registry = new InterceptorRegistry();
        config.addInterceptors(registry);
        return ReflectionTestUtils.invokeMethod(registry, "getInterceptors");
    }
}
