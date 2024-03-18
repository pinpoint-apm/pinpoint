/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;
import com.navercorp.pinpoint.plugin.spring.web.javax.interceptor.ServletRequestAttributeUtils;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServletRequestAttributeUtilsTest {
    private static final String SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEY = SpringWebMvcConstants.SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEYS[0];
    private static final String SPRING_MVC_URI_USER_INPUT_ATTRIBUTE_KEY = SpringWebMvcConstants.SPRING_MVC_URI_USER_INPUT_ATTRIBUTE_KEYS[0];

    @Test
    void extractAttribute() {
        ServletRequest servletRequestMock = mock(ServletRequest.class);
        when(servletRequestMock.getAttribute(SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEY)).thenReturn("/foo");
        when(servletRequestMock.getAttribute(SPRING_MVC_URI_USER_INPUT_ATTRIBUTE_KEY)).thenReturn("/bar");

        String value = ServletRequestAttributeUtils.extractAttribute(servletRequestMock, SpringWebMvcConstants.SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEYS);
        assertEquals("/foo", value);

        value = ServletRequestAttributeUtils.extractAttribute(servletRequestMock, SpringWebMvcConstants.SPRING_MVC_URI_USER_INPUT_ATTRIBUTE_KEYS);
        assertEquals("/bar", value);

        final String[] dummyKeyArray = new String[]{"item-0", "item-1"};
        value = ServletRequestAttributeUtils.extractAttribute(servletRequestMock, dummyKeyArray);
        assertEquals(null, value);
    }
}