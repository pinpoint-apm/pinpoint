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

package com.navercorp.pinpoint.test.junit4;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.test.ListenableDataSender;
import com.navercorp.pinpoint.test.MockApplicationContext;

import org.apache.thrift.TBase;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.TBaseRecorder;

/**
 * @author hyungil.jeong
 */
@RunWith(value = PinpointJUnit4ClassRunner.class)
public abstract class BasePinpointTest {

    private volatile TBaseRecorder<? extends TBase<?, ?>> tBaseRecorder;
    private volatile ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final SpanFactory spanFactory = new SpanFactory();

    protected List<SpanEventBo> getCurrentSpanEvents() {
        List<SpanEventBo> spanEvents = new ArrayList<SpanEventBo>();
        for (TBase<?, ?> span : this.tBaseRecorder) {
            if (span instanceof SpanEvent) {
                SpanEvent spanEvent = (SpanEvent)span;
                SpanEventBo spanEventBo = spanFactory.buildSpanEventBo(spanEvent);
                spanEvents.add(spanEventBo);
            }
        }
        return spanEvents;
    }

    protected List<SpanBo> getCurrentRootSpans() {
        List<SpanBo> rootSpans = new ArrayList<SpanBo>();
        for (TBase<?, ?> span : this.tBaseRecorder) {
            if (span instanceof Span) {
                SpanBo spanBo = spanFactory.buildSpanBo((Span) span);
                rootSpans.add(spanBo);
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
        MockApplicationContext mockApplicationContext = testContext.getMockApplicationContext();

        DataSender spanDataSender = mockApplicationContext.getSpanDataSender();
        if (spanDataSender instanceof ListenableDataSender) {
            ListenableDataSender listenableDataSender = (ListenableDataSender) spanDataSender;

            final TBaseRecorder tBaseRecord = new TBaseRecorder();

            listenableDataSender.setListener(new ListenableDataSender.Listener() {
                @Override
                public boolean handleSend(TBase<?, ?> data) {
                    return tBaseRecord.add(data);
                }
            });
            setTBaseRecorder(tBaseRecord);
        }

        ServerMetaDataRegistryService serverMetaDataRegistryService = mockApplicationContext.getServerMetaDataRegistryService();
        this.setServerMetaDataRegistryService(serverMetaDataRegistryService);
    }
}
