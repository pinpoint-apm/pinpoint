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

package com.navercorp.pinpoint.thrift.dto;

import com.navercorp.pinpoint.io.SpanVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanVersionTest {

    @Test
    public void supportedVersionRange() {
        Assert.assertTrue(SpanVersion.supportedVersionRange(SpanVersion.TRACE_V1));
        Assert.assertTrue(SpanVersion.supportedVersionRange(SpanVersion.TRACE_V2));

        Assert.assertFalse(SpanVersion.supportedVersionRange((byte) -1));
        Assert.assertFalse(SpanVersion.supportedVersionRange((byte) 100));
    }
}