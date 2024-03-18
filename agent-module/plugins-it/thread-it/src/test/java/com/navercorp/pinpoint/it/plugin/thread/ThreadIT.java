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
package com.navercorp.pinpoint.it.plugin.thread;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import org.junit.jupiter.api.Test;
import test.two.MockRunnable;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@Dependency({PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-thread-plugin"})
@PinpointConfig("pinpoint-thread-test.config")
public class ThreadIT {

    private static final String THREAD_ASYNC = "THREAD_ASYNC";

    @Test
    public void test() throws Exception {
        Thread thread = new Thread(new MockRunnable());
        thread.start();
        thread.join(1000);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event(THREAD_ASYNC, MockRunnable.class.getConstructor()));
        verifier.verifyTrace(event("ASYNC", "Asynchronous Invocation"));
        verifier.verifyTrace(event(THREAD_ASYNC, MockRunnable.class.getMethod("run")));
    }
}
