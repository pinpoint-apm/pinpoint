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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Taejin Koo
 */
public class StreamConnectionManager {

    private static final long DEFAULT_RECONNECT_DELAY = 5000;
    private static final long DEFAULT_AGENT_CHECk_DELAY = 10000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final PinpointWebSocketResponseAggregator responseAggregator;
    private final AgentService agentService;

    private final Timer timer;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    private final Object lock = new Object();
    private final AtomicBoolean onReconnectTimerTask = new AtomicBoolean(false);
    private final ConcurrentHashMap<AgentInfo, ReconnectProperties> reconnectJobRepository = new ConcurrentHashMap<AgentInfo, ReconnectProperties>();

    private final AtomicBoolean onAgentCheckTimerTask = new AtomicBoolean(false);
    private final List<String> defaultAgentIdList = new CopyOnWriteArrayList<String>();

    public StreamConnectionManager(PinpointWebSocketResponseAggregator responseAggregator, AgentService agentService, Timer timer) {
        this.responseAggregator = responseAggregator;
        this.agentService = agentService;

        this.timer = timer;
    }

    public void close() {
        synchronized (lock) {
            isStopped.compareAndSet(false, true);

            onReconnectTimerTask.set(false);
            reconnectJobRepository.clear();

            onAgentCheckTimerTask.set(false);
            defaultAgentIdList.clear();
        }
    }

    public void addReconnectJob(AgentInfo agentInfo, TBase commandObject, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) {
        logger.info("addReconnectJob. applicationName:{}, agent:{}", agentInfo.getApplicationName(), agentInfo.getAgentId());

        ReconnectProperties reconnectProperties = new ReconnectProperties(commandObject, messageListener, stateChangeListener);
        synchronized (lock) {
            if (isStopped.get()) {
                return;
            }

            reconnectJobRepository.put(agentInfo, reconnectProperties);
            boolean turnOn = onReconnectTimerTask.compareAndSet(false, true);
            if (turnOn) {
                timer.newTimeout(new ReconnectTimerTask(), DEFAULT_RECONNECT_DELAY, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void removeReconnectJob(AgentInfo agentInfo) {
        logger.info("removeReconnectJob. applicationName:{}, agent:{}", agentInfo.getApplicationName(), agentInfo.getAgentId());

        synchronized (lock) {
            reconnectJobRepository.remove(agentInfo);
        }
    }

    public void startAgentCheckJob() {
        logger.info("startAgentCheckJob. applicationName:{}", responseAggregator.getApplicationName());

        boolean turnOn = onAgentCheckTimerTask.compareAndSet(false, true);
        if (turnOn) {
            timer.newTimeout(new AgentCheckTimerTask(), DEFAULT_AGENT_CHECk_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    private static class ReconnectProperties {
        private final TBase commandObject;
        private final ClientStreamChannelMessageListener messageListener;
        private final StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener;

        public ReconnectProperties(TBase commandObject, com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) {
            this.commandObject = commandObject;
            this.messageListener = messageListener;
            this.stateChangeListener = stateChangeListener;
        }
    }

    private class ReconnectTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("ReconnectTimerTask started.");

            try {
                // need to divide lock.
                synchronized (lock) {
                    Iterator<Map.Entry<AgentInfo, ReconnectProperties>> iterator = reconnectJobRepository.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<AgentInfo, ReconnectProperties> entry = iterator.next();
                        AgentInfo agentInfo = entry.getKey();
                        ReconnectProperties reconnectProperties = entry.getValue();

                        AgentInfo newAgentInfo = agentService.getAgentInfo(agentInfo.getApplicationName(), agentInfo.getAgentId());
                        if (newAgentInfo != null) {
                            ClientStreamChannelContext clientStreamChannelContext = agentService.openStream(newAgentInfo, reconnectProperties.commandObject, reconnectProperties.messageListener, reconnectProperties.stateChangeListener);
                            if (clientStreamChannelContext != null && clientStreamChannelContext.getCreateFailPacket() == null) {
                                iterator.remove();
                            }
                        }
                    }

                    if (reconnectJobRepository.size() == 0) {
                        boolean turnOff = onReconnectTimerTask.compareAndSet(true, false);
                    }
                }
            } finally {
                if (timer != null && onReconnectTimerTask.get() && !isStopped.get()) {
                    timer.newTimeout(this, DEFAULT_RECONNECT_DELAY, TimeUnit.MILLISECONDS);
                }
            }
        }

    }

    private class AgentCheckTimerTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("AgentCheckTimerTask started.");

            try {
                String applicationName = responseAggregator.getApplicationName();
                List<AgentInfo> agentInfoList = agentService.getAgentInfoList(applicationName);
                for (AgentInfo agentInfo : agentInfoList) {
                    String agentId = agentInfo.getAgentId();
                    if (!defaultAgentIdList.contains(agentId)) {
                        responseAggregator.addAgent(agentInfo);
                        defaultAgentIdList.add(agentId);
                    }
                }
            } finally {
                if (timer != null && onAgentCheckTimerTask.get() && !isStopped.get()) {
                    timer.newTimeout(this, DEFAULT_AGENT_CHECk_DELAY, TimeUnit.MILLISECONDS);
                }
            }
        }

    }

}
