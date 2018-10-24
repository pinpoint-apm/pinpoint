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

package com.navercorp.pinpoint.test.junit4;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.test.ListenableDataSender;

import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.TBaseRecorder;

/**
 * @author hyungil.jeong
 */
@RunWith(value = PinpointJUnit4ClassRunner.class)
public abstract class BasePinpointTest {

    private volatile TBaseRecorder<?> tBaseRecorder;
    private volatile ServerMetaDataRegistryService serverMetaDataRegistryService;

    protected List<SpanEvent> getCurrentSpanEvents() {
        List<SpanEvent> spanEvents = new ArrayList<SpanEvent>();
        for (Object value : this.tBaseRecorder) {
            if (value instanceof SpanChunk) {
                final SpanChunk spanChunk = (SpanChunk) value;
                for (SpanEvent tSpanEvent : spanChunk.getSpanEventList()) {
                    SpanEvent spanEvent = tSpanEvent;
                    spanEvents.add(spanEvent);
                }
            }
        }
        return spanEvents;
    }

    protected List<Span> getCurrentRootSpans() {
        List<Span> rootSpans = new ArrayList<Span>();
        for (Object value : this.tBaseRecorder) {
            if (value instanceof Span) {
                Span span = (Span) value;
                rootSpans.add(span);
            }
        }
        return rootSpans;
    }

    protected ServerMetaData getServerMetaData() {
        return this.serverMetaDataRegistryService.getServerMetaData();
    }

    private void setTBaseRecorder(TBaseRecorder tBaseRecorder) {
        this.tBaseRecorder = tBaseRecorder;
    }

    private void setServerMetaDataRegistryService(ServerMetaDataRegistryService serverMetaDataRegistryService) {
        this.serverMetaDataRegistryService = serverMetaDataRegistryService;
    }

    public void setup(TestContext testContext) {
        DefaultApplicationContext mockApplicationContext = testContext.getDefaultApplicationContext();

        DataSender spanDataSender = mockApplicationContext.getSpanDataSender();
        if (spanDataSender instanceof ListenableDataSender) {
            ListenableDataSender listenableDataSender = (ListenableDataSender) spanDataSender;

            final TBaseRecorder<Object> tBaseRecord = new TBaseRecorder<Object>();

            listenableDataSender.setListener(new ListenableDataSender.Listener() {
                @Override
                public boolean handleSend(Object data) {
                    return tBaseRecord.add(data);
                }
            });
            setTBaseRecorder(tBaseRecord);
        }

        ServerMetaDataRegistryService serverMetaDataRegistryService = mockApplicationContext.getServerMetaDataRegistryService();
        this.setServerMetaDataRegistryService(serverMetaDataRegistryService);
    }
}
