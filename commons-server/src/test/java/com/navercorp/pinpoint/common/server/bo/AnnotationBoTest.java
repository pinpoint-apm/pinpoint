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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.serializer.AnnotationSerializer;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author emeroad
 */
public class AnnotationBoTest {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    
    private AnnotationSerializer serializer = new AnnotationSerializer();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testGetVersion() throws Exception {

    }

    @Test
    public void testSetVersion() throws Exception {

    }

    @Test
    public void testWriteValue() throws Exception {
        AnnotationBo annotation1 = new AnnotationBo();
        annotation1.setKey(AnnotationKey.API.getCode());

        final String value = RandomStringUtils.random(RandomUtils.nextInt(20));
        annotation1.setByteValue(value.getBytes(UTF_8));
        AnnotationBo annotation = annotation1;
//        int bufferSize = bo.getBufferSize();

        Buffer buffer = new AutomaticBuffer(128);
        this.serializer.writeAnnotation(annotation, buffer);


        Buffer deprecatedBuffer = new AutomaticBuffer(128);
        annotation.writeValue(deprecatedBuffer);
        Assert.assertArrayEquals(buffer.getBuffer(), deprecatedBuffer.getBuffer());

        AnnotationBo bo2 = new AnnotationBo();
        buffer.setOffset(0);
        bo2.readValue(buffer);
        Assert.assertEquals(annotation.getKey(), bo2.getKey());
        Assert.assertEquals(annotation.getValueType(), bo2.getValueType());
        Assert.assertArrayEquals(annotation.getByteValue(), bo2.getByteValue());
    }


}
