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
package com.navercorp.pinpoint.web.realtime.activethread.count.websocket;

import com.google.gson.Gson;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.Fetcher;
import com.navercorp.pinpoint.web.realtime.activethread.count.dao.FetcherFactory;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ActiveThreadCountResponse;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import com.navercorp.pinpoint.web.websocket.ActiveThreadCountHandler;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
class ActiveThreadCountHandlerImpl extends ActiveThreadCountHandler implements PinpointWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(ActiveThreadCountHandlerImpl.class);

    private static final long MAX_CONNECTION_WAITING_MILLIS = TimeUnit.SECONDS.toMillis(5);

    private final FetcherFactory<ClusterKey, ATCSupply> fetcherFactory;
    private final AgentLookupService agentLookupService;
    @Nullable private final TimerTaskDecoratorFactory timerTaskDecoratorFactory;
    private final Flux<Long> tickProvider;

    private final Gson gson = new Gson();

    ActiveThreadCountHandlerImpl(
            FetcherFactory<ClusterKey, ATCSupply> fetcherFactory,
            AgentLookupService agentLookupService,
            @Nullable TimerTaskDecoratorFactory timerTaskDecoratorFactory,
            Flux<Long> tickProvider
    ) {
        super(null);
        this.fetcherFactory = Objects.requireNonNull(fetcherFactory, "fetcherFactory");
        this.agentLookupService = Objects.requireNonNull(agentLookupService, "agentLookupService");
        this.timerTaskDecoratorFactory = timerTaskDecoratorFactory;
        this.tickProvider = Objects.requireNonNull(tickProvider, "tickProvider");
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        logger.info("ATC Connection Established. session: {}", session);
        SessionContext.newContext(session, this.timerTaskDecoratorFactory);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        logger.info("ATC Connection Closed. session: {}, status: {}", session, status);
        SessionContext.dispose(session);
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    protected void handleActiveThreadCount(WebSocketSession session, String applicationName) {
        logger.info("ATC Requested. session: {}, applicationName: {}", session, applicationName);
        refresh(session, applicationName);
    }

    private void refresh(WebSocketSession session, String applicationName) {
        SessionContext.runWithLockedContext(session, ctx -> refreshWithContext(ctx, session, applicationName));
    }

    @GuardedBy("ATCSessionContext")
    private void refreshWithContext(SessionContext ctx, WebSocketSession session, String applicationName) {
        final Consumer<Long> handler = makeTickHandler(ctx, session, applicationName);
        ctx.setSubscription(this.tickProvider.subscribe(handler));
    }

    private Consumer<Long> makeTickHandler(SessionContext ctx, WebSocketSession session, String applicationName) {
        final List<ClusterKey> agentKeys = this.agentLookupService.getRecentAgents(applicationName);
        final TickHandler handler = new TickHandler(session, applicationName, agentKeys, getFetchers(agentKeys));
        final Runnable decoratedHandler = decorateHandler(ctx.getTaskDecorator(), handler);
        return t -> decoratedHandler.run();
    }

    private List<Fetcher<ATCSupply>> getFetchers(List<ClusterKey> agentKeys) {
        final List<Fetcher<ATCSupply>> fetchers = new ArrayList<>(agentKeys.size());
        for (final ClusterKey agentKey : agentKeys) {
            final Fetcher<ATCSupply> fetcher = getFetcher(agentKey);
            fetchers.add(fetcher);
        }
        return fetchers;
    }

    private Fetcher<ATCSupply> getFetcher(ClusterKey agentKey) {
        try {
            return this.fetcherFactory.getFetcher(agentKey);
        } catch (Exception e) {
            logger.error("Failed to get fetcher for {}", agentKey, e);
            return Fetcher.constant(getErrorSupply(agentKey));
        }
    }

    private ATCSupply getErrorSupply(ClusterKey agentKey) {
        final ATCSupply s = new ATCSupply();
        s.setCollectorId("UNKNOWN");
        s.setValues(List.of());
        s.setMessage(ATCSupply.Message.WEB_ERROR);
        s.setApplicationName(agentKey.getApplicationName());
        s.setAgentId(agentKey.getAgentId());
        s.setStartTimestamp(agentKey.getStartTimestamp());
        return s;
    }

    private static Runnable decorateHandler(TimerTaskDecorator decorator, TimerTask target) {
        if (decorator == null) {
            return target;
        }
        return decorator.decorate(target);
    }

    private class TickHandler extends TimerTask {

        private final WebSocketSession session;
        private final long sessionCreatedAt;
        private final String applicationName;
        private final List<ClusterKey> clusterKeys;
        private final List<Fetcher<ATCSupply>> fetchers;

        final AtomicLong tickHandlerSpan = new AtomicLong(10);

        public TickHandler(
                WebSocketSession session,
                String applicationName,
                List<ClusterKey> clusterKeys,
                List<Fetcher<ATCSupply>> fetchers
        ) {
            this.session = session;
            this.sessionCreatedAt = SessionContext.get(session).getSessionCreatedAt();
            this.applicationName = applicationName;
            this.clusterKeys = clusterKeys;
            this.fetchers = fetchers;
        }

        @Override
        public void run() {
            send(makeResponse());
            trySelfRefresh();
        }

        private void trySelfRefresh() {
            if (tickHandlerSpan.decrementAndGet() == 0) {
                refresh(session, applicationName);
            }
        }

        private ActiveThreadCountResponse makeResponse() {
            final long now = System.currentTimeMillis();
            final ActiveThreadCountResponse response = new ActiveThreadCountResponse(applicationName, now);
            for (int i = 0; i < clusterKeys.size(); i++) {
                final ClusterKey clusterKey = clusterKeys.get(i);
                final Fetcher<ATCSupply> fetcher = fetchers.get(i);
                final ATCSupply supply = fetcher.fetch();
                putAgent(response, clusterKey, supply);
            }
            return response;
        }

        private void putAgent(ActiveThreadCountResponse response, ClusterKey agentKey, ATCSupply supply) {
            if (supply != null && !supply.getValues().isEmpty()) {
                response.putSuccessAgent(agentKey, supply.getValues());
            } else {
                final long connectUntil = sessionCreatedAt + MAX_CONNECTION_WAITING_MILLIS;
                response.putFailureAgent(agentKey, supply, connectUntil);
            }
        }

        private void send(ActiveThreadCountResponse response) {
            try {
                final String message = gson.toJson(response);
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.warn("Failed to send message. session: {}", session);
            }
        }

    }

}
