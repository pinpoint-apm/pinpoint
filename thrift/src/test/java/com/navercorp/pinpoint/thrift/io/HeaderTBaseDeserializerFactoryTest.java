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

package com.navercorp.pinpoint.thrift.io;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

public class HeaderTBaseDeserializerFactoryTest {

    @Test
    public void optionTest1() {

        HeaderTBaseSerializerFactory factory = new HeaderTBaseSerializerFactory();
        Assert.assertTrue(factory.isSafetyGuaranteed());
    }

    @Test
    public void optionTest2() {
        HeaderTBaseSerializerFactory factory = new HeaderTBaseSerializerFactory(true, 1);
        Assert.assertTrue(factory.isSafetyGuaranteed());
    }

    @Test
    public void optionTest() {
        HeaderTBaseSerializerFactory factory = new HeaderTBaseSerializerFactory(false, 1);
        Assert.assertFalse(factory.isSafetyGuaranteed());
    }
}
