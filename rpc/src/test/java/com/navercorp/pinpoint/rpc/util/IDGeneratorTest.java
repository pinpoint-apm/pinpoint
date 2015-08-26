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

package com.navercorp.pinpoint.rpc.util;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.util.IDGenerator;

public class IDGeneratorTest {

    @Test
    public void generatorTest() {
        IDGenerator generator = new IDGenerator();

        Assert.assertEquals(1, generator.generate());
        Assert.assertEquals(2, generator.generate());
        Assert.assertEquals(3, generator.generate());

        generator = new IDGenerator(2, 3);

        Assert.assertEquals(2, generator.generate());
        Assert.assertEquals(5, generator.generate());
        Assert.assertEquals(8, generator.generate());
    }

}
