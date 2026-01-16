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

package com.navercorp.pinpoint.bootstrap.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ProfilableClassFilterTest {

    @Test
    public void testEmptyConfiguration() {
        ProfilableClassFilter filter = new ProfilableClassFilter("");

        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/MyClass"));
        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controller/MyController"));
    }

    @Test
    public void testExactMatch() {
        ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.SomeSpecificClass");

        Assertions.assertTrue(filter.filter("com/navercorp/pinpoint/SomeSpecificClass"));
        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/SomeOtherClass"));
    }

    @Test
    public void testSubpackageFiltering() {
        ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.controller.*");

        Assertions.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/TestController1"));
        Assertions.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/subcontroller/InnerController"));
        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/otherpackage/NotController"));
    }

    @Test
    public void testUnexpectedClassNames() {
        ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.*");

        Assertions.assertFalse(filter.filter("random/unknown/path"));
        Assertions.assertFalse(filter.filter("com/random/pinpoint/path"));
    }

    @Test
    public void testIsProfilableClassWithNoConfiguration() throws IOException {
        ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass");

        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controllers/MyController"));
        Assertions.assertFalse(filter.filter("net/spider/king/wang/Jjang"));
        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb2/controller/MyController"));
        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb2/MyClass"));
    }

    /**
     * <pre>
     * configuration is
     * profile.package.include=com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass
     * </pre>
     *
     * @throws IOException
     */
    @Test
    public void testIsProfilableClass() throws IOException {
        ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass");

        Assertions.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/MyClass"));
        Assertions.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/MyController"));
        Assertions.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/customcontroller/MyCustomController"));

        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/MyUnknownClass"));
        Assertions.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controller2/MyController"));
    }

}