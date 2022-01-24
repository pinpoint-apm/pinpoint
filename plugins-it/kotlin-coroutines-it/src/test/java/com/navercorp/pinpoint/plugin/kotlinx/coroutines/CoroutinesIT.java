/*
 * Copyright 2022 NAVER Corp.
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
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;
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
    private static final String RESUME_WITH_METHOD = ".resumeWith(";
    private static final String SCHEDULE_RESUME_METHOD = ".scheduleResumeAfterDelay(";
    private static final String ASYNC_INVOCATION = "Asynchronous Invocation";

    @Test
    public void executeRunBlockingWitoutContext() {
        final boolean activeAsync = false;

        // This test has 1 ~ 4 executed Async Invocation
        // This test has 4 executed runSafely()
        CoroutinesLaunch coroutinesLaunch = new CoroutinesLaunch();
        coroutinesLaunch.executeWithRunBlocking();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        List<String> executedMethod = verifier.getExecutedMethod();

        AtomicInteger index = new AtomicInteger();

        //         runBlocking(context) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        //         runBlocking(context) {
        assertResumeWith(executedMethod, index, activeAsync);

        //        val job1 = async(CoroutineName("first")) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        //        val job2 = launch(CoroutineName("second")) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));

        //        delay(10L) // job1
        assertResumeWithAndSchedule(executedMethod, index, activeAsync);

        //        delay(5L) // job2
        assertResumeWithAndSchedule(executedMethod, index, activeAsync);

        //        println("Hello World 1")  // job1
        assertResumeWith(executedMethod, index, activeAsync);
        //        println("Hello World 2")  // job2
        assertResumeWith(executedMethod, index, activeAsync);
        //    println("Hello all of jobs") // rootjob
        assertResumeWith(executedMethod, index, activeAsync);
    }

    @Test
    public void executeRunBlocking() {
        final boolean activeAsync = true;

        CoroutinesLaunch coroutinesLaunch = new CoroutinesLaunch();
        CoroutineDispatcher dispatcher = Dispatchers.getDefault();
        coroutinesLaunch.executeWithRunBlocking((CoroutineContext) dispatcher);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.awaitTraceCount(17, 10L, 1000L);

        List<String> executedMethod = verifier.getExecutedMethod();

        AtomicInteger index = new AtomicInteger();

        //         runBlocking(context) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        //         runBlocking(context) {
        assertResumeWith(executedMethod, index, activeAsync);

        //        val job1 = async(CoroutineName("first")) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        //        val job2 = launch(CoroutineName("second")) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));

        //    println("Hello all of jobs") // rootjob
        assertResumeWith(executedMethod, index, activeAsync);

        //        delay(10L) // job1
        assertResumeWithAndSchedule(executedMethod, index, activeAsync);

        //        delay(5L) // job2
        assertResumeWithAndSchedule(executedMethod, index, activeAsync);

        //        println("Hello World 1")  // job1
        assertResumeWith(executedMethod, index, activeAsync);
        //        println("Hello World 2")  // job2
        assertResumeWith(executedMethod, index, activeAsync);
    }

    private void assertResumeWithAndSchedule(List<String> executedMethod, AtomicInteger index, boolean activeAsync) {
        if (activeAsync) {
            Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(ASYNC_INVOCATION));
        }
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(RESUME_WITH_METHOD));
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(SCHEDULE_RESUME_METHOD));
    }

    private void assertResumeWith(List<String> executedMethod, AtomicInteger index, boolean activeAsync) {
        if (activeAsync) {
            Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(ASYNC_INVOCATION));
        }
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(RESUME_WITH_METHOD));
    }

    private void assertRunblockingDispatch(List<String> executedMethod, AtomicInteger index) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).equals(ASYNC_INVOCATION));
        // run dispatchedContinuation
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(RESUME_WITH_METHOD));
    }


    private void assertFirstDispatch(List<String> executedMethod, AtomicInteger index) {
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(DISPATCH_METHOD));
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).equals(ASYNC_INVOCATION));
        // run dispatchedContinuation
        Assert.assertTrue(executedMethod.get(index.getAndIncrement()).contains(RESUME_WITH_METHOD));
    }

    private boolean assertExecutedCount(String[] executeActualMethod, String expectedActualMethod, int expectedCount) {
        long count = Arrays.stream(executeActualMethod).filter(e -> e.contains(expectedActualMethod)).count();
        return count == expectedCount;
    }

}
