/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
@RestController
public class ReactorNettyPluginTestController {

    @RequestMapping(value = "/client/echo", method = RequestMethod.GET)
    @ResponseBody
    public String clientEcho() {
        return "Welcome";
    }

    @RequestMapping(value = "/client/get", method = RequestMethod.GET)
    @ResponseBody
    public String clientGet() {
        HttpClient client = HttpClient.create().port(80);
        String response = client.get().uri("https://www.google.com?foo=bar").responseContent().aggregate().asString().block();
        return response;
    }

    @RequestMapping(value = "/client/local", method = RequestMethod.GET)
    @ResponseBody
    public String clientError(HttpServletRequest request) {
        HttpClient client = HttpClient.create().port(request.getLocalPort());
        String response = client.get().uri("/client/echo").responseContent().aggregate().asString().block();
        return response;
    }

    @RequestMapping(value = "/client/post", method = RequestMethod.GET)
    @ResponseBody
    public String clientPost() {
        HttpClient client = HttpClient.create().port(80);
        HttpClientResponse response = client.post().uri("https://www.google.com/").send(ByteBufFlux.fromString(Mono.just("hello"))).response().block();
        return response.toString();
    }

    @RequestMapping(value = "/client/unknown", method = RequestMethod.GET)
    @ResponseBody
    public String clientError() {
        HttpClient client = HttpClient.create().port(80);
        String response = client.get().uri("http://fjalkjdlfaj.com").responseContent().aggregate().asString().block();
        return response;
    }
}
