package com.pinpoint.test.plugin;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
class SpringBoot3WebfluxPluginHandler {
    public Mono<ServerResponse> routerHandler(ServerRequest req) {
        return ServerResponse.ok().body(Mono.just(req.pathVariable("param")), String.class);
    }

    public Mono<ServerResponse> useUserInputUri(ServerRequest req) {
        req.exchange().getAttributes().put("pinpoint.metric.uri-template", "/test");
        return ServerResponse.ok().body(Mono.just("Test"), String.class);
    }

}
