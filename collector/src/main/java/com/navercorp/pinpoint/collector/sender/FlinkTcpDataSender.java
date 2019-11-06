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
package com.navercorp.pinpoint.collector.sender;

import com.navercorp.pinpoint.io.request.FlinkRequest;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.FlinkHeaderTBaseSerializer;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkTcpDataSender extends TcpDataSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FlinkHeaderTBaseSerializer flinkHeaderTBaseSerializer;
    private final FlinkRequestFactory flinkRequestFactory;

    public FlinkTcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory, FlinkHeaderTBaseSerializer serializer, FlinkRequestFactory flinkRequestFactory) {
        super(name, host, port, clientFactory);

        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name must not be empty.");
        }
        if (StringUtils.isEmpty(host)) {
            throw new IllegalArgumentException("host must not be empty.");
        }
        Objects.requireNonNull(clientFactory, "clientFactory");
        this.flinkHeaderTBaseSerializer = Objects.requireNonNull(serializer, "serializer");
        this.flinkRequestFactory = Objects.requireNonNull(flinkRequestFactory, "clientFactory");
    }

    @Override
    public boolean send(Object data) {
        if (!(data instanceof TBase<?, ?>)) {
            logger.info("unknown message:{}", data);
            return false;
        }
        TBase<?, ?> message = (TBase<?, ?>) data;
        FlinkRequest flinkRequest = flinkRequestFactory.createFlinkRequest(message, new HashMap<String, String>(0));
        return executor.execute(flinkRequest);
    }

    @Override
    protected void sendPacket(Object flinkRequest) {
        try {
            if (flinkRequest instanceof FlinkRequest) {
                byte[] copy = flinkHeaderTBaseSerializer.serialize((FlinkRequest) flinkRequest);
                if (copy == null) {
                    return;
                }
                doSend(copy);
            } else {
                logger.error("sendPacket fail. invalid dto type:{}", flinkRequest.getClass());
                return;
            }
        } catch (Exception e) {
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }
}
