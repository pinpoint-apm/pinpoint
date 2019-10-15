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

package com.navercorp.pinpoint.common.profiler.trace;

import java.util.List;

import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;

public class StaticFieldLookUpTest {

    @Test
    public void testFindServiceType() {
        StaticFieldLookUp<ServiceType> staticFieldLookUp = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> lookup = staticFieldLookUp.lookup();

        Assert.assertTrue(findType(lookup, ServiceType.UNKNOWN_DB));
        Assert.assertTrue(findType(lookup, ServiceType.UNDEFINED));
    }

    @Test
    public void testNotFindServiceType() {
        StaticFieldLookUp<ServiceType> staticFieldLookUp = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> lookup = staticFieldLookUp.lookup();

        final int SERVER_CATEGORY_MAX = 1999;
        ServiceType notExist = ServiceTypeFactory.of(SERVER_CATEGORY_MAX, "test", "test");
        Assert.assertFalse(findType(lookup, notExist));
    }


    @Test
    public void testFindString() {
        StaticFieldLookUp<String> staticFieldLookUp = new StaticFieldLookUp<String>(StaticFieldLookUpTestClass.class, String.class);
        List<String> lookup = staticFieldLookUp.lookup();

        Assert.assertTrue(findType(lookup, StaticFieldLookUpTestClass.string1));
    }


    @Test
    public void testNotFindString() {
        StaticFieldLookUp<String> staticFieldLookUp = new StaticFieldLookUp<String>(StaticFieldLookUpTestClass.class, String.class);
        List<String> lookup = staticFieldLookUp.lookup();

        Assert.assertFalse(findType(lookup, "notExist"));
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