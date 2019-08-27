/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.reader.core;

import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author HyunGil Jeong
 */
public class CheckSumTest {

    @Test
    public void computeAndParse() {
        String randomValue = UUID.randomUUID().toString();
        CheckSum checkSum = CheckSum.compute(CheckSum.getCurrentVersion(), randomValue);
        CheckSum parsedCheckSum = CheckSum.parse(checkSum.toString());
        Assert.assertEquals(checkSum.getVersion(), parsedCheckSum.getVersion());
        Assert.assertEquals(checkSum.getCheckSum(), parsedCheckSum.getCheckSum());
    }
}
