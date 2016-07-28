/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.filter;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SequenceSpanEventFilterTest {

    @Test
    public void testFilter_accept() throws Exception {
        SpanEventFilter filter = new SequenceSpanEventFilter(100);

        final SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setSequence((short)11);

        Assert.assertEquals(filter.filter(spanEventBo), SpanEventFilter.ACCEPT);

    }


    @Test
    public void testFilter_reject() throws Exception {
        SpanEventFilter filter = new SequenceSpanEventFilter(10);

        final SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setSequence((short)11);

        Assert.assertEquals(filter.filter(spanEventBo), SpanEventFilter.REJECT);

    }

    @Test
    public void testFilter_max() throws Exception {
        new SequenceSpanEventFilter(Short.MAX_VALUE);

        try {
            new SequenceSpanEventFilter(Short.MAX_VALUE + 1);
            Assert.fail();
        } catch (Exception e) {
        }

    }

}