/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HBaseClientVersionTest {

    @Test
    public void acceptVersion() {
        HBaseClientVersion v1 = HBaseClientVersion.V1;
        Assertions.assertTrue(v1.acceptVersion("1.4.0"));
        Assertions.assertFalse(v1.acceptVersion("2.0.0"));
    }


    @Test
    public void getHBaseVersion() {
        Assertions.assertEquals(HBaseClientVersion.V1, HBaseClientVersion.getHBaseVersion("0.9.0"));
        Assertions.assertEquals(HBaseClientVersion.V1, HBaseClientVersion.getHBaseVersion("1.4.0"));

        Assertions.assertEquals(HBaseClientVersion.V2, HBaseClientVersion.getHBaseVersion("2.0.0"));

        Assertions.assertEquals(null, HBaseClientVersion.getHBaseVersion("4.0.0"));

    }
}