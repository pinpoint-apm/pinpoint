/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyVersionFilterTest {

    @Test
    public void filter() {
        DependencyVersionFilter filter = new DependencyVersionFilter();
        assertFalse(filter.filter("5.0.5.RELEASE"));
        assertTrue(filter.filter("5.0.0.RC2"));
        assertTrue(filter.filter("5.0.0.RC1"));
        assertTrue(filter.filter("5.0.0.M2"));

        assertFalse(filter.filter("4.0.0"));
        assertTrue(filter.filter("4.0.0-rc1"));
        assertTrue(filter.filter("4.0.0-beta3"));
        assertTrue(filter.filter("4.0.0-beta2"));
        assertTrue(filter.filter("4.0.0-beta1"));
        assertTrue(filter.filter("4.0.0-alpha3"));

        assertTrue(filter.filter("0.3.2-patch11"));
        assertTrue(filter.filter("0.3.2-patch1"));
        assertTrue(filter.filter("0.3.2-test3"));

        assertTrue(filter.filter("3.0.0-milestone2"));
        assertTrue(filter.filter("1.15.0-rc"));
        assertTrue(filter.filter("1.14.1-beta"));
        assertTrue(filter.filter("1.14.1-beta-2"));
        assertTrue(filter.filter("1.4.0-rc.7"));
    }
}