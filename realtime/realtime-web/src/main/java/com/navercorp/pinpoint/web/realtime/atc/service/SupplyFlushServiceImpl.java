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
package com.navercorp.pinpoint.web.realtime.atc.service;

import com.google.gson.Gson;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.util.PrefixThreadFactory;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.atc.dto.ATCSession;
import com.navercorp.pinpoint.web.realtime.atc.dto.ActiveThreadCountResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public class SupplyFlushServiceImpl implements SupplyFlushService {

    private static final Logger logger = LogManager.getLogger(SupplyFlushServiceImpl.class);
    private static final long MAX_CONNECTION_WAITING_NANOS = TimeUnit.SECONDS.toNanos(5);

    private final ATCSessionRepository sessionRepository;
    private final ATCValueDao valueDao;
    private final ExecutorService executor;

    private final Gson gson = new Gson();

    public SupplyFlushServiceImpl(ATCSessionRepository sessionRepository, ATCValueDao valueDao, int numWorker) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
        this.valueDao = Objects.requireNonNull(valueDao, "valueDao");

        final ThreadFactory threadFactory = new PrefixThreadFactory("ATC-Supply-Publisher");
        this.executor = new ThreadPoolExecutor(
                numWorker, numWorker, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), threadFactory
        );
    }

    @Override
    public void flush() {
        try {
            final long now = System.nanoTime();
            final long nowMs = System.currentTimeMillis();
            final List<ATCSession> sessions = this.sessionRepository.getSessions();
            for (final ATCSession session: sessions) {
                executor.execute(() -> flush(session, now, nowMs));
            }
        } catch (Exception e) {
            logger.error("Failed to flush", e);
        }
    }

    private void flush(ATCSession session, long now, long nowMs) {
        try {
            final String applicationName = session.getDemandApplicationName();
            final List<ClusterKey> agents = this.valueDao.getActiveAgents(applicationName);
            final ActiveThreadCountResponse response = new ActiveThreadCountResponse(applicationName, nowMs);
            for (final ClusterKey agent: agents) {
                addInResponse(response, agent, now, session.getCreatedAt());
            }
            final TextMessage message = makeTextMessage(response);
            sendMessage(session, message);
        } catch (Exception e) {
            logger.error("Failed to flush session: {}", session, e);
        }
    }

    private void addInResponse(ActiveThreadCountResponse response, ClusterKey agent, long now, long sessionCreatedAt) {
        final List<Integer> values = valueDao.query(agent, now);
        final String agentId = agent.getAgentId();
        if (values == null) {
            response.putActiveThreadCount(agentId, -1, getValueNotFoundMessage(now - sessionCreatedAt), null);
        } else {
            response.putActiveThreadCount(agentId, 0, "OK", values);
        }
    }

    private static String getValueNotFoundMessage(long ageNanos) {
        if (ageNanos > MAX_CONNECTION_WAITING_NANOS) {
            return "FAIL";
        } else {
            return "CONNECTING";
        }
    }

    private TextMessage makeTextMessage(ActiveThreadCountResponse response) {
        final String serialized = gson.toJson(response);
        return new TextMessage(serialized);
    }

    private void sendMessage(ATCSession session, TextMessage message) {
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            logger.warn("Failed to send message. session: {}", session);
        }
    }

}
