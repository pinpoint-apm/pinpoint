/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.HistogramSchema;
import com.navercorp.pinpoint.common.ServiceType;

public class StaticFieldLookUpTest {

    @Test
    public void testFindServiceType() throws IllegalAccessException {
        StaticFieldLookUp<ServiceType> staticFieldLookUp = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> lookup = staticFieldLookUp.lookup();

        Assert.assertTrue(findType(lookup, ServiceType.CUBRID));
        Assert.assertTrue(findType(lookup, ServiceType.UNDEFINED));
    }

    @Test
    public void testNotFindServiceType() throws IllegalAccessException {
        StaticFieldLookUp<ServiceType> staticFieldLookUp = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> lookup = staticFieldLookUp.lookup();

        ServiceType notExist = new ServiceType(Short.MIN_VALUE, "test", "test", HistogramSchema.NORMAL_SCHEMA);
        Assert.assertFalse(findType(lookup, notExist));
    }


    @Test
    public void testFindDisplayArgumentMatcher() throws IllegalAccessException {
        StaticFieldLookUp<DisplayArgumentMatcher> staticFieldLookUp = new StaticFieldLookUp<DisplayArgumentMatcher>(DefaultDisplayArgument.class, DisplayArgumentMatcher.class);
        List<DisplayArgumentMatcher> lookup = staticFieldLookUp.lookup();

        Assert.assertTrue(findType(lookup, DefaultDisplayArgument.UNKNOWN_DB_MATCHER));
    }


    @Test
    public void testNotFindDisplayArgumentMatcher() throws IllegalAccessException {
        StaticFieldLookUp<DisplayArgumentMatcher> staticFieldLookUp = new StaticFieldLookUp<DisplayArgumentMatcher>(DefaultDisplayArgument.class, DisplayArgumentMatcher.class);
        List<DisplayArgumentMatcher> lookup = staticFieldLookUp.lookup();

        DisplayArgumentMatcher notExist = new DisplayArgumentMatcher(ServiceType.UNDEFINED, AnnotationKeyMatcher.NOTHING_MATCHER);
        Assert.assertFalse(findType(lookup, notExist));
    }


    private static <T> boolean findType(List<T> lookup, T find) {
        for (T serviceType : lookup) {
            if (serviceType == find) {
                return true;
            }
        }
        return false;
    }
}