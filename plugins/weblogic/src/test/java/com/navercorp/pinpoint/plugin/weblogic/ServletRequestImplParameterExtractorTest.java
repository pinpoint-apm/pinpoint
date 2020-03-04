/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.weblogic;

import org.junit.Assert;
import org.junit.Test;
import weblogic.servlet.internal.ServletRequestImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServletRequestImplParameterExtractorTest {
    @Test
    public void extractParameter() {
        String queryString = "a=1&b=2";
        ServletRequestImpl servletRequest = mock(ServletRequestImpl.class);
        when(servletRequest.getQueryString()).thenReturn(queryString);
        ServletRequestImplParameterExtractor extractor = new ServletRequestImplParameterExtractor(100, 100);
        String normalizedString = extractor.extractParameter(servletRequest);
        Assert.assertEquals(queryString, normalizedString);
    }
}