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

package com.navercorp.pinpoint.common.bo;

import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class AnnotationBoTest {
    @Test
    public void testGetVersion() throws Exception {

    }

    @Test
    public void testSetVersion() throws Exception {

    }

    @Test
    public void testWriteValue() throws Exception {
        AnnotationBo bo = new AnnotationBo();
        bo.setKey(AnnotationKey.API.getCode());
        bo.setByteValue("value".getBytes("UTF-8"));
//        int bufferSize = bo.getBufferSize();

        Buffer buffer = new AutomaticBuffer(128);
        bo.writeValue(buffer);

        AnnotationBo bo2 = new AnnotationBo();
        buffer.setOffset(0);
        bo2.readValue(buffer);
        Assert.assertEquals(bo.getKey(), bo2.getKey());
        Assert.assertEquals(bo.getValueType(), bo2.getValueType());
        Assert.assertArrayEquals(bo.getByteValue(), bo2.getByteValue());
    }

}
