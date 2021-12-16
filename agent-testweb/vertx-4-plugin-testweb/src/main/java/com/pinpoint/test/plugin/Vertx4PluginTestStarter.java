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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Router;

import java.util.concurrent.TimeUnit;

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
            routingContext.response().end("Welcome pinpoint vert.x HTTP server test.");
        });
        router.get("/request").handler(routingContext -> {
            request(80, "naver.com", "/");
            routingContext.response().end("Request http://naver.com:80/");
        });
        router.get("/request/local").handler(routingContext -> {
            request(18080, "localhost", "/");
            routingContext.response().end("Request http://localhost:18080/");
        });
        router.get("/request/https").handler(routingContext -> {
            request(443, "naver.com", "/");
            routingContext.response().end("Request http://naver.com:80/");
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

        vertx.createHttpServer().requestHandler(router).listen(18080, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 18080");
            } else {
                startPromise.fail(http.cause());
            }
        });
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
            request(80, "naver.com", "/");
            request.response().end("Run on context request.");
        });
    }

    private void sleep(int waiteSeconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waiteSeconds));
        } catch (InterruptedException e) {
        }
    }

    public void request(int port, String host, String uri) {
        final HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, port, host, uri, new Handler<AsyncResult<HttpClientRequest>>() {
            @Override
            public void handle(AsyncResult<HttpClientRequest> asyncResult) {
                System.out.println("## Result=" + asyncResult.result());

            }
        });
    }
}