/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.io.request;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class AttributeMapTest {

    private static final AttributeKey<String> STRING_ATTRIBUTE_KEY = new DefaultAttributeKey<String>("STRING_ATTRIBUTE_KEY", "HELLO");

    @Test
    public void setGetTest() {
        DefaultServerRequest defaultServerRequest = new DefaultServerRequest(EmptyMessage.INSTANCE);

        String value = defaultServerRequest.getAttribute(STRING_ATTRIBUTE_KEY);
        Assert.assertEquals(STRING_ATTRIBUTE_KEY.getDefaultValue(), value);

        String hi = "HI";
        defaultServerRequest.setAttribute(STRING_ATTRIBUTE_KEY, hi);
        value = defaultServerRequest.getAttribute(STRING_ATTRIBUTE_KEY);
        Assert.assertEquals("HI", value);
    }

}
