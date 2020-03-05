/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.pluginit.utils;


import fi.iki.elonen.NanoHTTPD;

import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class WebServer extends NanoHTTPD {
    public static final String VERSION = "org.nanohttpd:nanohttpd:2.3.1";
    public static final String LOCAL_HOST = "localhost";

    public WebServer(String hostname, int port) {
        super(hostname, port);
    }

    public static WebServer cleanup(WebServer webServer) {
        if (webServer != null) {
            webServer.stop();
        }
        return webServer;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        Response response = newFixedLengthResponse(parameters.toString());
        return response;
    }


    public static WebServer newTestWebServer() throws Exception {

        final int port = SocketUtils.findAvailableTcpPort(21000);
        final WebServer webServer = new WebServer(LOCAL_HOST, port);
        webServer.start();
        return webServer;
    }

    public String getHostAndPort() {
        return this.getHostname() + ":" + this.getListeningPort();
    }

    public String getCallHttpUrl() {
        return "http://" + getHostAndPort();
    }


}
