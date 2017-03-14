/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSpanRecorderTest {

    @Test
    public void testRecordApiId() throws Exception {
        Span span = new Span();

        StringMetaDataService stringMetaDataService = Mockito.mock(StringMetaDataService.class);
        SqlMetaDataService sqlMetaDataService = Mockito.mock(SqlMetaDataService.class);


        SpanRecorder recorder = new DefaultSpanRecorder(span, true, true, stringMetaDataService, sqlMetaDataService);

        final int API_ID = 1000;
        recorder.recordApiId(API_ID);

        Assert.assertEquals("API ID", span.getApiId(), API_ID);
    }
}