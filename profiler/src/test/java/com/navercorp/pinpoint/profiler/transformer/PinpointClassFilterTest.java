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

package com.navercorp.pinpoint.profiler.transformer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinpointClassFilterTest {

    @Test
    public void doFilter() {

        ClassFileFilter filter = new PinpointClassFilter();

        Assertions.assertEquals(ClassFileFilter.CONTINUE, filter.accept(null, "java/test", null, null, null));
        Assertions.assertEquals(ClassFileFilter.CONTINUE, filter.accept(null, "javax/test", null, null, null));
        Assertions.assertEquals(ClassFileFilter.CONTINUE, filter.accept(null, "test", null, null, null));
    }

    @Test
    public void doFilter_Package() {

        ClassFileFilter filter = new PinpointClassFilter();

        Assertions.assertEquals(ClassFileFilter.SKIP, filter.accept(null, "com/navercorp/pinpoint/", null, null, null));
    }

    @Test
    public void doFilter_Package_exclude() {

        ClassFileFilter filter = new PinpointClassFilter();

        Assertions.assertEquals(ClassFileFilter.CONTINUE, filter.accept(null, "com/navercorp/pinpoint/web/", null, null, null));

    }
}