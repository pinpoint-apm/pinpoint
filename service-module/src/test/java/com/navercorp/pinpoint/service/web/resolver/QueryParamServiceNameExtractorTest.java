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
class QueryParamServiceNameExtractorTest {

    @Mock
    private HttpServletRequest request;

    private QueryParamServiceNameExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new QueryParamServiceNameExtractor();
    }

    @Test
    void extract_WithValidParameter_ReturnsServiceName() {
        String expectedServiceName = "queryService";

        when(request.getParameter(ServiceConstants.KEY)).thenReturn(expectedServiceName);

        String result = extractor.extract(request);

        assertThat(result).isEqualTo(expectedServiceName);
    }

    @Test
    void extract_WithoutParameter_ReturnsNull() {
        when(request.getParameter(ServiceConstants.KEY)).thenReturn(null);

        String result = extractor.extract(request);

        assertThat(result).isNull();
    }

    @Test
    void extract_WithEmptyParameter_ReturnsEmptyString() {
        when(request.getParameter(ServiceConstants.KEY)).thenReturn("");

        String result = extractor.extract(request);

        assertThat(result).isEmpty();
    }

    @Test
    void extract_WithDifferentParameterKey_ChecksCorrectKey() {
        String expectedServiceName = "myService";

        when(request.getParameter(ServiceConstants.KEY)).thenReturn(expectedServiceName);

        String result = extractor.extract(request);

        assertThat(result).isEqualTo(expectedServiceName);
    }
}
