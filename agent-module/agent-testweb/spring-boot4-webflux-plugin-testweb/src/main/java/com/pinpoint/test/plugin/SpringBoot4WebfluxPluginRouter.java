package com.pinpoint.test.plugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
class SpringBoot4WebfluxPluginRouter {
    @Bean
    public RouterFunction<ServerResponse> routes(SpringBoot4WebfluxPluginHandler handler) {
        return route(GET("/router-handler/{param}"), handler::routerHandler)
                .andRoute(GET("/use-userinput-uri"), handler::useUserInputUri);
    }
}
