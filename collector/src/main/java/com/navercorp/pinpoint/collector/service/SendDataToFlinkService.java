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
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author minwoo.jung
 */
public abstract class SendDataToFlinkService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile List<FlinkTcpDataSender> flinkTcpDataSenderList = new CopyOnWriteArrayList<>();
    private final AtomicInteger callCount = new AtomicInteger(1);

    protected void sendData(TBase<?, ?> data) {
        FlinkTcpDataSender tcpDataSender = roundRobinTcpDataSender();
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

    private FlinkTcpDataSender roundRobinTcpDataSender() {
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
        this.flinkTcpDataSenderList = new CopyOnWriteArrayList<FlinkTcpDataSender>(flinkTcpDataSenderList);
    }
}
