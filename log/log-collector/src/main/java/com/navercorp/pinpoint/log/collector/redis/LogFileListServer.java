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
package com.navercorp.pinpoint.log.collector.redis;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.log.collector.service.LogConsumerService;
import com.navercorp.pinpoint.log.vo.FileKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
class LogFileListServer implements InitializingBean {

    private final String HOSTNAME = getHostname();

    private final Logger logger = LogManager.getLogger(LogFileListServer.class);
    private final ScheduledExecutorService broadcastExecutor;
    private final LogConsumerService service;
    private final Duration connectionBroadcastingPeriod;
    private final ChannelProviderRepository channelProviderRepository;

    private PubChannel pubChannel;

    LogFileListServer(
            ScheduledExecutorService broadcastExecutor,
            LogConsumerService service,
            ChannelProviderRepository channelProviderRepository,
            Duration connectionBroadcastingPeriod
    ) {
        this.broadcastExecutor = Objects.requireNonNull(broadcastExecutor, "broadcastExecutor");
        this.service = Objects.requireNonNull(service, "service");
        this.channelProviderRepository = Objects.requireNonNull(channelProviderRepository, "channelProviderRepository");
        this.connectionBroadcastingPeriod =
                Objects.requireNonNullElse(connectionBroadcastingPeriod, Duration.ofSeconds(10));
    }

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(HOSTNAME)) {
            throw new RuntimeException("Failed to initialize LogFileListServer: invalid hostname");
        }
        this.pubChannel = channelProviderRepository.getPubChannel(URI.create("kv:PT20S:log:files:" + HOSTNAME));
        scheduleNextBroadcasting();
    }

    private void scheduleNextBroadcasting() {
        this.broadcastExecutor.schedule(
                this::broadcastLogFiles,
                this.connectionBroadcastingPeriod.toNanos(),
                TimeUnit.NANOSECONDS
        );
    }

    private void broadcastLogFiles() {
        try {
            broadcastLogFiles0();
        } catch (Exception e) {
            logger.error("Failed to broadcast log files", e);
        } finally {
            scheduleNextBroadcasting();
        }
    }

    private void broadcastLogFiles0() {
        StringBuilder b = new StringBuilder();
        List<FileKey> keys = this.service.getFileKeys();
        for (FileKey key: keys) {
            b.append(key).append("\r\n");
        }
        this.pubChannel.publish(BytesUtils.toBytes(b.toString()));
        logger.trace("Broadcast {} log files", keys.size());
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }

}
