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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-coroutines.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-kotlin-coroutines-plugin"})
@Dependency({
        "log4j:log4j:[1.2.17]",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:[1.0.1,)"
})
@JvmVersion(8)
public class CoroutinesIT {

    private static final String DISPATCH_METHOD = ".dispatch(";
    private static final String RUN_METHOD = ".runSafely(";
    private static final String ASYNC_INVOCATION = "Asynchronous Invocation";

    @Test
    public void executeOneLaunchBlockTest() {
        int minimumExpectedCount = 7;
        int launchBlockCount = 1;
        int expectedExecutedRunSafelyCount = 2;

        // This test has 1 ~ 2 executed Async Invocation
        // This test has 2 executed runSafely()
        CoroutinesLaunch coroutinesLaunch = new CoroutinesLaunch();
        coroutinesLaunch.execute("pinpoint-test");

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        List<String> executedMethod = verifier.getExecutedMethod();

        AtomicInteger index = new AtomicInteger();

        // dispatch runblocking
        Assert.assertTrue(executedMethod.size() >= minimumExpectedCount);
        assertFirstDispatch(executedMethod, index);
        for (int i = 0; i < launchBlockCount; i++) {
            // dispatch launch job
            Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        }

        final String[] executeActualMethods = Arrays.copyOfRange(executedMethod.toArray(new String[0]), index.get(), executedMethod.size());
        Assert.assertTrue(assertExecutedCount(executeActualMethods, RUN_METHOD, expectedExecutedRunSafelyCount));
        Assert.assertTrue(assertExecutedCount(executeActualMethods, ASYNC_INVOCATION, executeActualMethods.length - expectedExecutedRunSafelyCount));
    }

    @Test
    public void executeTwoLaunchBlockTest() {
        int minimumExpectedCount = 10;
        int launchBlockCount = 2;
        int expectedExecutedRunSafelyCount = 4;


        // This test has 1 ~ 4 executed Async Invocation
        // This test has 4 executed runSafely()
        CoroutinesLaunch coroutinesLaunch = new CoroutinesLaunch();
        coroutinesLaunch.execute2("pinpoint-test");

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        List<String> executedMethod = verifier.getExecutedMethod();

        AtomicInteger index = new AtomicInteger();

        // dispatch runblocking
        Assert.assertTrue(executedMethod.size() >= minimumExpectedCount);
        assertFirstDispatch(executedMethod, index);
        for (int i = 0; i < launchBlockCount; i++) {
            // dispatch launch job
            Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        }

        final String[] executeActualMethods = Arrays.copyOfRange(executedMethod.toArray(new String[0]), index.get(), executedMethod.size());
        Assert.assertTrue(assertExecutedCount(executeActualMethods, RUN_METHOD, expectedExecutedRunSafelyCount));
        Assert.assertTrue(assertExecutedCount(executeActualMethods, ASYNC_INVOCATION, executeActualMethods.length - expectedExecutedRunSafelyCount));
    }


    private void assertFirstDispatch(List<String> executedMethod, AtomicInteger index) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).equals(ASYNC_INVOCATION));
        // run dispatchedContinuation
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(RUN_METHOD));
    }

    private boolean assertExecutedCount(String[] executeActualMethod, String expectedActualMethod, int expectedCount) {
        long count = Arrays.stream(executeActualMethod).filter(e -> e.contains(expectedActualMethod)).count();
        return count == expectedCount;
    }

    @Test
    public void executeCurrentThreadTest() {
        int expectedCount = 2;

        // This test has 0 executed Async Invocation
        // This test has 0 executed runSafely()
        CoroutinesLaunch coroutinesLaunch = new CoroutinesLaunch();
        coroutinesLaunch.executeParentDispatcher("pinpoint-test");

        // executes 2 times dispatch
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        List<String> executedMethod = verifier.getExecutedMethod();
        Assert.assertEquals(expectedCount, executedMethod.size());
        Assert.assertTrue(executedMethod.get(0).contains(DISPATCH_METHOD));
        Assert.assertTrue(executedMethod.get(1).contains(DISPATCH_METHOD));
    }

}
