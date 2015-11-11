/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.receiver.CommandSerializer;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.receiver.ProfilerStreamCommandService;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class ActiveThreadCountService implements ProfilerRequestCommandService, ProfilerStreamCommandService {

    private static final long DEFAULT_FLUSH_DELAY = 1000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();

    private final StreamChannelStateChangeEventHandler stateChangeEventHandler = new ActiveThreadCountStreamChannelStateChangeEventHandler();
    private final HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("ActiveThreadCountService-Timer", 100, TimeUnit.MILLISECONDS, 512);
    private final long flushDelay;
    private final AtomicBoolean onTimerTask = new AtomicBoolean(false);

    private final List<ServerStreamChannel> streamChannelRepository = new CopyOnWriteArrayList<ServerStreamChannel>();


    private static final List<SlotType> ACTIVE_THREAD_SLOTS_ORDER = new ArrayList<SlotType>();
    static {
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.FAST);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.NORMAL);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.SLOW);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.VERY_SLOW);
    }

    private final ActiveTraceLocator activeTraceLocator;
    private final int activeThreadSlotsCount;
    private final HistogramSchema histogramSchema = HistogramSchema.NORMAL_SCHEMA;

    public ActiveThreadCountService(ActiveTraceLocator activeTraceLocator) {
        this(activeTraceLocator, DEFAULT_FLUSH_DELAY);
    }

    public ActiveThreadCountService(ActiveTraceLocator activeTraceLocator, long flushDelay) {
        if (activeTraceLocator == null) {
            throw new NullPointerException("activeTraceLocator");
        }
        this.activeTraceLocator = activeTraceLocator;
        this.activeThreadSlotsCount = ACTIVE_THREAD_SLOTS_ORDER.size();

        this.flushDelay = flushDelay;
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TCmdActiveThreadCount.class;
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase activeThreadCountObject) {
        if (activeThreadCountObject == null) {
            throw new NullPointerException("activeThreadCountObject may not be null.");
        }

        return getActiveThreadCountResponse();
    }

    @Override
    public StreamCode streamCommandService(TBase tBase, ServerStreamChannelContext streamChannelContext) {
        logger.info("streamCommandService object:{}, streamChannelContext:{}", tBase, streamChannelContext);
        streamChannelContext.getStreamChannel().addStateChangeEventHandler(stateChangeEventHandler);
        return StreamCode.OK;
    }

    private TCmdActiveThreadCountRes getActiveThreadCountResponse() {
        Map<SlotType, IntAdder> mappedSlot = new LinkedHashMap<SlotType, IntAdder>(activeThreadSlotsCount);
        for (SlotType slotType : ACTIVE_THREAD_SLOTS_ORDER) {
            mappedSlot.put(slotType, new IntAdder(0));
        }

        long currentTime = System.currentTimeMillis();

        List<ActiveTraceInfo> collectedActiveTraceInfo = activeTraceLocator.collect();
        for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
            HistogramSlot slot = histogramSchema.findHistogramSlot((int) (currentTime - activeTraceInfo.getStartTime()), false);
            mappedSlot.get(slot.getSlotType()).incrementAndGet();
        }

        List<Integer> activeThreadCount = new ArrayList<Integer>(activeThreadSlotsCount);
        for (IntAdder statusCount : mappedSlot.values()) {
            activeThreadCount.add(statusCount.get());
        }

        TCmdActiveThreadCountRes response = new TCmdActiveThreadCountRes();
        response.setHistogramSchemaType(histogramSchema.getTypeCode());
        response.setActiveThreadCount(activeThreadCount);
        response.setTimeStamp(System.currentTimeMillis());

        return response;
    }

    private static class IntAdder {
        private int value = 0;

        public IntAdder(int defaultValue) {
            this.value = defaultValue;
        }

        public int incrementAndGet() {
            return ++value;
        }

        public int get() {
            return this.value;
        }
    }

    private class ActiveThreadCountStreamChannelStateChangeEventHandler implements StreamChannelStateChangeEventHandler<ServerStreamChannel> {

        @Override
        public void eventPerformed(ServerStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) throws Exception {
            synchronized (lock) {
                switch (updatedStateCode) {
                    case CONNECTED:
                        streamChannelRepository.add(streamChannel);
                        boolean turnOn = onTimerTask.compareAndSet(false, true);
                        if (turnOn) {
                            timer.newTimeout(new ActiveThreadCountTimerTask(), flushDelay, TimeUnit.MILLISECONDS);
                        }
                        break;
                    case CLOSED:
                    case ILLEGAL_STATE:
                        boolean removed = streamChannelRepository.remove(streamChannel);
                        if (removed && streamChannelRepository.size() == 0) {
                            boolean turnOff = onTimerTask.compareAndSet(true, false);
                        }
                        break;
                }
            }
        }

        @Override
        public void exceptionCaught(ServerStreamChannel streamChannel, StreamChannelStateCode updatedStateCode, Throwable e) {
        }

    }

    private class ActiveThreadCountTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("ActiveThreadCountService timer started.");

            try {
                TCmdActiveThreadCountRes activeThreadCountResponse = getActiveThreadCountResponse();
                for (ServerStreamChannel serverStreamChannel : streamChannelRepository) {
                    byte[] payload = SerializationUtils.serialize(activeThreadCountResponse, CommandSerializer.SERIALIZER_FACTORY, null);
                    if (payload != null) {
                        serverStreamChannel.sendData(payload);
                    }
                }
            } finally {
                if (timer != null && onTimerTask.get()) {
                    timer.newTimeout(this, flushDelay, TimeUnit.MILLISECONDS);
                }
            }
        }

    }
}