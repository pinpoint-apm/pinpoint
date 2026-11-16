/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.api.JvmVersion;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PluginForkedTest;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * org.asynchttpclient 3.x, which requires Java 11.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(11)
@Dependency({"org.asynchttpclient:async-http-client:[3.0.0],[3.0.14,3.max]",
        WebServer.VERSION, PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-ning-asynchttpclient-plugin"})
public class NingAsyncHttpClient_3_x_IT {

    @AutoClose("stop")
    private static WebServer webServer;

    @BeforeAll
    public static void beforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @Test
    public void test() throws Exception {
        AsyncHttpClientTester.executeAndVerify(webServer);
    }
}
