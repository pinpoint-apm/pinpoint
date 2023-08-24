package com.navercorp.pinpoint.log.web;

import com.navercorp.pinpoint.log.web.controller.LogControllerConfig;
import com.navercorp.pinpoint.log.web.websocket.LogWebSocketConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        LogWebSocketConfig.class,
        LogControllerConfig.class
})
public class LogWebConfig {

}
