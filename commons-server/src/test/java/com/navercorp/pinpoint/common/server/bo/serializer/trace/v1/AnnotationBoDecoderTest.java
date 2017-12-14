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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author emeroad
 */
public class AnnotationBoDecoderTest {

    private AnnotationSerializer serializer = new AnnotationSerializer();

    private AnnotationBoDecoder annotationBoDecoder = new AnnotationBoDecoder();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testWriteValue() throws Exception {
        final AnnotationBo annotation = new AnnotationBo();
        annotation.setKey(AnnotationKey.API.getCode());

        final String value = RandomStringUtils.random(RandomUtils.nextInt(0, 20));
        annotation.setValue(value);

        final Buffer buffer = new AutomaticBuffer(128);
        this.serializer.writeAnnotationList(Lists.newArrayList(annotation), buffer);

        buffer.setOffset(0);
        List<AnnotationBo> decode = annotationBoDecoder.decode(buffer);
        Assert.assertEquals(decode.size(), 1);
        AnnotationBo decodedAnnotation = decode.get(0);
        Assert.assertEquals(annotation.getKey(), decodedAnnotation.getKey());
        Assert.assertEquals(annotation.getValue(), decodedAnnotation.getValue());

    }


}
