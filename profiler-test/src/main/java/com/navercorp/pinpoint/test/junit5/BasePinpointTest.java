/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.junit5;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.test.ListenableDataSender;
import com.navercorp.pinpoint.test.Recorder;
import com.navercorp.pinpoint.test.SpanRecorder;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public abstract class BasePinpointTest {

    private volatile SpanRecorder recorder;
    private volatile ServerMetaDataRegistryService serverMetaDataRegistryService;

    protected List<SpanEvent> getCurrentSpanEvents() {
        return this.recorder.getCurrentSpanEvents();
    }

    protected List<Span> getCurrentRootSpans() {
        return this.recorder.getCurrentSpans();
    }

    protected ServerMetaData getServerMetaData() {
        return this.serverMetaDataRegistryService.getServerMetaData();
    }

    private void setRecorder(Recorder<SpanType> recorder) {
        this.recorder = new SpanRecorder(recorder);
    }

    private void setServerMetaDataRegistryService(ServerMetaDataRegistryService serverMetaDataRegistryService) {
        this.serverMetaDataRegistryService = serverMetaDataRegistryService;
    }

    public void setup(TestContext testContext) {
        DefaultApplicationContext mockApplicationContext = testContext.getDefaultApplicationContext();

        DataSender<SpanType> spanDataSender = mockApplicationContext.getSpanDataSender();
        if (spanDataSender instanceof ListenableDataSender) {
            ListenableDataSender<SpanType> listenableDataSender = (ListenableDataSender<SpanType>) spanDataSender;

            final Recorder<SpanType> recorder = new Recorder<>();

            listenableDataSender.setListener(new ListenableDataSender.Listener<SpanType>() {
                @Override
                public boolean handleSend(SpanType data) {
                    return recorder.add(data);
                }
            });
            setRecorder(recorder);
        }

        ServerMetaDataRegistryService serverMetaDataRegistryService = mockApplicationContext.getServerMetaDataRegistryService();
        this.setServerMetaDataRegistryService(serverMetaDataRegistryService);
    }
}
