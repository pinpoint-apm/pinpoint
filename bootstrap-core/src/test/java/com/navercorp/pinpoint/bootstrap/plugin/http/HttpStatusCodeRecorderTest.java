/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.http;

import com.navercorp.pinpoint.bootstrap.config.HttpStatusCodeErrors;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * @author jaehong.kim
 */
public class HttpStatusCodeRecorderTest {

    @Test
    public void record() throws Exception {
        final SpanRecorder spanRecorder = mock(SpanRecorder.class);

        HttpStatusCodeErrors errors = new HttpStatusCodeErrors(Arrays.asList("5xx", "401", "402"));
        HttpStatusCodeRecorder recorder = new HttpStatusCodeRecorder(errors);

        recorder.record(spanRecorder, 500);
        recorder.record(spanRecorder, 200);
        recorder.record(spanRecorder, 404);

        // illegal argument.
        recorder.record(null, 500);
        recorder.record(spanRecorder, 0);
        recorder.record(spanRecorder, -1);
        recorder.record(spanRecorder, 999);
    }
}