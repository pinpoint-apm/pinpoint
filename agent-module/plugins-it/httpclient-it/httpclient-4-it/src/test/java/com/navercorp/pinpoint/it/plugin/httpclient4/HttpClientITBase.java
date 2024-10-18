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

package com.navercorp.pinpoint.it.plugin.httpclient4;

import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class HttpClientITBase {
    public static WebServer webServer;

    @BeforeAll
    public static void beforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();

    }

    @AfterAll
    public static void afterClass() throws Exception {
        webServer = WebServer.cleanup(webServer);
    }

    public String getAddress() {
        return webServer.getCallHttpUrl();
    }

    public static String getHostPort() {
        return webServer.getHostAndPort();
    }

    public String getCallerApp(HttpResponse response) {
        if (response == null) {
            return null;
        }
        final Header callerHeader = response.getFirstHeader(WebServer.CALLER_RESPONSE_HEADER_NAME);
        if (callerHeader != null) {
            return callerHeader.getValue();
        }
        return null;
    }

}
