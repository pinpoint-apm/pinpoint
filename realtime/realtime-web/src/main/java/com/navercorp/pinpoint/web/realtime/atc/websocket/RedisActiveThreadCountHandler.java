package com.navercorp.pinpoint.web.realtime.atc.websocket;

import com.navercorp.pinpoint.web.realtime.atc.service.DemandRegisterService;
import com.navercorp.pinpoint.web.websocket.ActiveThreadCountHandler;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

public class RedisActiveThreadCountHandler extends ActiveThreadCountHandler implements PinpointWebSocketHandler {

    private static final Logger logger = LogManager.getLogger(RedisActiveThreadCountHandler.class);

    private final DemandRegisterService demandRegisterService;

    public RedisActiveThreadCountHandler(DemandRegisterService demandRegisterService) {
        super(null);
        this.demandRegisterService = Objects.requireNonNull(demandRegisterService, "demandRegisterService");
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        logger.info("ATC Connection Established. session: {}", session);
        this.demandRegisterService.registerSession(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        logger.info("ATC Connection Closed. session: {}, status: {}", session, status);
        this.demandRegisterService.unregisterSession(session);
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    protected void handleActiveThreadCount(WebSocketSession webSocketSession, String applicationName) {
        this.demandRegisterService.registerDemandToSession(webSocketSession, applicationName);
    }

}