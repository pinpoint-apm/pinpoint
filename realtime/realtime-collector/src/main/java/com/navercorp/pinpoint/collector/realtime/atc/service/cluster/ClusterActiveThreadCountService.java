/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.collector.realtime.atc.service.cluster;

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.realtime.atc.service.ActiveThreadCountService;
import com.navercorp.pinpoint.collector.realtime.service.AgentCommandService;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import com.navercorp.pinpoint.util.ScheduleUtil;
import org.apache.thrift.TBase;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
public class ClusterActiveThreadCountService implements ActiveThreadCountService {

    private final ScheduledExecutorService closer = ScheduleUtil.makeScheduledExecutorService("closer");

    private final AgentCommandService commandService;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;
    private final long demandDurationMillis;

    public ClusterActiveThreadCountService(
            AgentCommandService commandService,
            DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory,
            long demandDurationMillis
    ) {
        this.commandService = Objects.requireNonNull(commandService, "commandService");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory");
        this.demandDurationMillis = demandDurationMillis;
    }

    @Override
    public void requestAsync(ClusterKey target, Consumer<List<Integer>> callback) throws Exception {
        final ClusterPoint<?> clusterPoint = commandService.findClusterPoint(target);
        if (clusterPoint == null) {
            return;
        }

        final TCmdActiveThreadCount command = new TCmdActiveThreadCount();
        final DesConsumer desConsumer = new DesConsumer(deserializerFactory, callback);
        final ClientStreamChannel clientChannel = commandService.request(clusterPoint, command, desConsumer);

        closer.schedule(() -> clientChannel.close(), this.demandDurationMillis, TimeUnit.MILLISECONDS);
    }

    private static class DesConsumer implements Consumer<StreamResponsePacket> {

        private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;
        private final Consumer<List<Integer>> delegate;

        public DesConsumer(
                DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory,
                Consumer<List<Integer>> delegate
        ) {
            this.deserializerFactory = deserializerFactory;
            this.delegate = delegate;
        }

        @Override
        public void accept(StreamResponsePacket packet) {
            final byte[] payload = packet.getPayload();
            TCmdActiveThreadCountRes res = this.deserialize(payload);
            if (res != null) {
                this.delegate.accept(res.getActiveThreadCount());
            }
        }

        private TCmdActiveThreadCountRes deserialize(byte[] payload) {
            final Message<TBase<?, ?>> message = SerializationUtils.deserialize(payload, deserializerFactory, null);
            if (message == null) {
                return null;
            }

            final TBase<?, ?> data = message.getData();
            if (data instanceof  TCmdActiveThreadCountRes) {
                return (TCmdActiveThreadCountRes) data;
            } else {
                return null;
            }
        }

    }
}
