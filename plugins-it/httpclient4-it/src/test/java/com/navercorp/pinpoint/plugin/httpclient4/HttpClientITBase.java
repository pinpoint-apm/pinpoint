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

package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;

public abstract class HttpClientITBase {

    private static com.navercorp.pinpoint.pluginit.utils.WebServer webServer;

    // ---------- For @BeforeSharedClass, @AfterSharedClass   //
    private static String CALL_URL;
    private static String HOST_PORT;


    public static String getCallUrl() {
        return CALL_URL;
    }

    public static void setCallUrl(String callUrl) {
        CALL_URL = callUrl;
    }

    public static String getHostPort() {
        return HOST_PORT;
    }

    public static void setHostPort(String hostPort) {
        HOST_PORT = hostPort;
    }
    // ---------- //

    @BeforeSharedClass
    public static void sharedSetUp() throws Exception {
        webServer = WebServer.newTestWebServer();
        setCallUrl(webServer.getCallHttpUrl());
        setHostPort(webServer.getHostAndPort());
    }

    @AfterSharedClass
    public static void sharedTearDown() {
        webServer = WebServer.cleanup(webServer);
    }

}
