/*
 * Copyright 2021 NAVER Corp.
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

package com.pinpoint.test.plugin;

import com.pinpoint.test.common.view.ApiLinkPage;
import com.pinpoint.test.common.view.HrefTag;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Vertx4PluginTestStarter extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        HttpServerOptions options = new HttpServerOptions();
        options.setIdleTimeout(1000);
        options.setSsl(true);
        SelfSignedCertificate selfSignedCertificate = SelfSignedCertificate.create();
        options.setKeyCertOptions(selfSignedCertificate.keyCertOptions());
        options.setTrustOptions(selfSignedCertificate.trustOptions());

        Router router = Router.router(vertx);

        router.get("/").handler(routingContext -> {
            routingContext.redirect("/main");
        });
        router.get("/reroute").handler(routingContext -> {
            routingContext.reroute("/main");
        });
        router.get("/main").handler(routingContext -> {
            List<Route> routes = router.getRoutes();
            routingContext.response().end(buildMain("Welcome pinpoint vert.x HTTP server test", routes));
        });

        router.get("/request").handler(routingContext -> {
            request(80, "naver.com", "/");
            routingContext.response().end("Request http://naver.com:80/");
        });
        router.get("/request/local").handler(routingContext -> {
            request(18080, "localhost", "/");
            routingContext.response().end("Request http://localhost:18080");
        });
        router.get("/request/https").handler(routingContext -> {
            request(443, "naver.com", "/");
            routingContext.response().end("Request https://naver.com:80/");
        });
        router.get("/noresponse").handler(routingContext -> {
        });
        router.get("/close").handler(routingContext -> {
            routingContext.response().close();
        });
        router.get("/connection/close").handler(routingContext -> {
            routingContext.request().connection().close();
        });
        router.get("/executeBlocking").handler(routingContext -> {
            executeBlocking(routingContext.request(), 1);
        });
        router.get("/executeBlocking/wait10s").handler(routingContext -> {
            executeBlocking(routingContext.request(), 10);
        });
        router.get("/executeBlocking/request").handler(routingContext -> {
            executeBlockingRequest(routingContext.request());
        });
        router.get("/runOnContext").handler(routingContext -> {
            runOnContext(routingContext.request(), 1);
        });
        router.get("/runOnContext/wait10s").handler(routingContext -> {
            runOnContext(routingContext.request(), 10);
        });
        router.get("/runOnContext/request").handler(routingContext -> {
            runOnContextRequest(routingContext.request());
        });
        router.get("/test/:arg1/*").handler(routingContext -> {
            String arg1 = routingContext.pathParam("arg1");
            routingContext.response().end(arg1);
        });
        router.get("/template/:arg1").handler(routingContext -> {
            String arg1 = routingContext.pathParam("arg1");
            routingContext.response().end(arg1);
        });
        router.get("/routinContextAttributeAdded").handler(routingContext -> {
            routingContext.put("pinpoint.metric.uri-template", "/test");
            routingContext.response().end("pinpoint.metric.uri-tempate = /test");
        });

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router).listen(18080, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port " + httpServer.actualPort());
            } else {
                startPromise.fail(http.cause());
            }
        });
    }

    private String buildMain(String title, List<Route> routes) {
        List<HrefTag> tags = routes.stream()
                .map(route -> HrefTag.of(route.getPath()))
                .collect(Collectors.toList());
        return new ApiLinkPage(title)
                .addHrefTag(tags)
                .build();
    }

    private void executeBlocking(HttpServerRequest request, final int waitSeconds) {
        vertx.executeBlocking(new Handler<Promise<Object>>() {
            @Override
            public void handle(Promise<Object> objectFuture) {
                sleep(waitSeconds);
                request.response().end("Execute blocking.");
            }
        }, false, null);
    }

    private void executeBlockingRequest(HttpServerRequest request) {
        vertx.executeBlocking(new Handler<Promise<Object>>() {
            @Override
            public void handle(Promise<Object> objectFuture) {
                request(80, "naver.com", "/");
                request.response().end("Execute blocking request.");
            }
        }, false, null);
    }

    private void runOnContext(HttpServerRequest request, final int waitSeconds) {
        vertx.runOnContext(aVoid -> {
            sleep(waitSeconds);
            request.response().end("Run on context");
        });

    }

    private void runOnContextRequest(HttpServerRequest request) {
        vertx.runOnContext(aVoid -> {
            request(80, "httpbin.org", "/");
            request.response().end("Run on context request.");
        });
    }

    private void sleep(int waiteSeconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waiteSeconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void request(int port, String host, String uri) {
        WebClient client = WebClient.create(vertx);
        client.get(port, host, uri).timeout(Long.MAX_VALUE).send()
                .onSuccess(event -> System.out.println("##Received response with status code=" + event.statusCode()))
                .onFailure(event -> System.out.println("##Something went wrong=" + event.getMessage()));
    }
}