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

/**
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
class DefaultServiceNameExtractorTest {

    @Mock
    private HttpServletRequest request;

    private DefaultServiceNameExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new DefaultServiceNameExtractor();
    }

    @Test
    void extract_AlwaysReturnsDefault() {
        String result = extractor.extract(request);

        assertThat(result).isEqualTo(ServiceConstants.DEFAULT);
    }

    @Test
    void extract_ReturnsDefaultValue() {
        String result = extractor.extract(request);

        assertThat(result).isEqualTo("DEFAULT");
    }

    @Test
    void extract_WithNullRequest_ReturnsDefault() {
        String result = extractor.extract(null);

        assertThat(result).isEqualTo(ServiceConstants.DEFAULT);
    }

    @Test
    void extract_MultipleCalls_ConsistentResult() {
        String result1 = extractor.extract(request);
        String result2 = extractor.extract(request);

        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isEqualTo(ServiceConstants.DEFAULT);
    }
}
