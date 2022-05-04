/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.sender.FlinkTcpDataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import org.apache.thrift.TBase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author minwoo.jung
 */
@Component
public class SendDataToFlinkService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private volatile List<TcpDataSender<TBase<?, ?>>> flinkTcpDataSenderList = new CopyOnWriteArrayList<>();
    private final AtomicInteger callCount = new AtomicInteger(1);

    protected void sendData(TBase<?, ?> data) {
        TcpDataSender<TBase<?, ?>> tcpDataSender = roundRobinTcpDataSender();
        if (tcpDataSender == null) {
            logger.warn("not send flink server. Because FlinkTcpDataSender is null.");
            return;
        }

        try {
            tcpDataSender.send(data);
            if (logger.isDebugEnabled()) {
                logger.debug("send to flinkserver : {}", data);
            }
        } catch (Exception e) {
            logger.error("Error sending to flink server. Caused:{}", e.getMessage(), e);
        }
    }

    private TcpDataSender<TBase<?, ?>> roundRobinTcpDataSender() {
        if (flinkTcpDataSenderList.isEmpty()) {
            return null;
        }

        int count = callCount.getAndIncrement();
        int tcpDataSenderIndex = count % flinkTcpDataSenderList.size();

        if (tcpDataSenderIndex < 0) {
            tcpDataSenderIndex = tcpDataSenderIndex * -1;
            callCount.set(0);
        }

        try {
            return flinkTcpDataSenderList.get(tcpDataSenderIndex);
        } catch (Exception e) {
            logger.warn("not get FlinkTcpDataSender", e);
        }

        return null;
    }

    public void replaceFlinkTcpDataSenderList(List<FlinkTcpDataSender> flinkTcpDataSenderList) {
        this.flinkTcpDataSenderList = new CopyOnWriteArrayList<>(flinkTcpDataSenderList);
    }
}
