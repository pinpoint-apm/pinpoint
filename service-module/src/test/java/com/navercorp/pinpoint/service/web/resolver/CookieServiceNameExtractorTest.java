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

package com.navercorp.pinpoint.service.web.resolver;

import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
class CookieServiceNameExtractorTest {

    @Mock
    private HttpServletRequest request;

    private CookieServiceNameExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new CookieServiceNameExtractor();
    }

    @Test
    void extract_WithValidCookie_ReturnsServiceName() {
        String expectedServiceName = "cookieService";
        Cookie[] cookies = {
                new Cookie("otherCookie", "value1"),
                new Cookie(ServiceConstants.KEY, expectedServiceName)
        };

        when(request.getCookies()).thenReturn(cookies);

        String result = extractor.extract(request);

        assertThat(result).isEqualTo(expectedServiceName);
    }

    @Test
    void extract_WithMatchingCookieFirst_ReturnsServiceName() {
        String expectedServiceName = "firstService";
        Cookie[] cookies = {
                new Cookie(ServiceConstants.KEY, expectedServiceName),
                new Cookie("otherCookie", "value1")
        };

        when(request.getCookies()).thenReturn(cookies);

        String result = extractor.extract(request);

        assertThat(result).isEqualTo(expectedServiceName);
    }

    @Test
    void extract_WithoutMatchingCookie_ReturnsNull() {
        Cookie[] cookies = {
                new Cookie("cookie1", "value1"),
                new Cookie("cookie2", "value2")
        };

        when(request.getCookies()).thenReturn(cookies);

        String result = extractor.extract(request);

        assertThat(result).isNull();
    }

    @Test
    void extract_WithNullCookies_ReturnsNull() {
        when(request.getCookies()).thenReturn(null);

        String result = extractor.extract(request);

        assertThat(result).isNull();
    }

    @Test
    void extract_WithEmptyCookies_ReturnsNull() {
        Cookie[] cookies = {};

        when(request.getCookies()).thenReturn(cookies);

        String result = extractor.extract(request);

        assertThat(result).isNull();
    }

    @Test
    void extract_WithMultipleCookies_ReturnsFirstMatch() {
        String expectedServiceName = "firstMatch";
        Cookie[] cookies = {
                new Cookie("cookie1", "value1"),
                new Cookie(ServiceConstants.KEY, expectedServiceName),
                new Cookie("cookie2", "value2")
        };

        when(request.getCookies()).thenReturn(cookies);

        String result = extractor.extract(request);

        assertThat(result).isEqualTo(expectedServiceName);
    }
}
