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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.StringTraceHeaderParser;
import com.navercorp.pinpoint.common.util.TraceHeader;

import java.util.UUID;

/**
 * @author emeroad
 */
public class StringTraceHeaderParserTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private StringTraceHeaderParser parser= new StringTraceHeaderParser();

    @Test
    public void getIdSize() {
        String test = "3ccb94f3-a8fe-4464-bfbd-d35490afab3d";
        logger.debug("idSize={}", test.length());
    }


    @Test
    public void createStringBaseTraceHeader() {
        createAndParser(UUID.randomUUID().toString(), 123, 345, 23423, (short) 22);
        createAndParser(UUID.randomUUID().toString(), -1, 2, 0, (short) 0);
        createAndParser(UUID.randomUUID().toString(), 234, 2, 0, (short) 0);
    }



    private void createAndParser(String uuid, int spanId, int pSpanId, int sampling, short flag) {
        String traceHeader = parser.createHeader(uuid, spanId, pSpanId, sampling, (short) flag);

        TraceHeader header = parser.parseHeader(traceHeader);
        Assert.assertEquals("id", uuid, header.getId());
        Assert.assertEquals("spanId", String.valueOf(spanId), header.getSpanId());
        Assert.assertEquals("pSpanId", String.valueOf(pSpanId), header.getParentSpanId());
        Assert.assertEquals("sampling", String.valueOf(sampling), header.getSampling());
        Assert.assertEquals("flag", String.valueOf(flag), header.getFlag());
        logger.debug("{}, parse:" + header);
    }
}
