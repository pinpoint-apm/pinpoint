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

package com.navercorp.pinpoint.plugin.process;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-process-plugin"})
@PinpointConfig("pinpoint-process-test.config")
public class ProcessIT {

    private long getPid(Process process) {
        Field pidField = null;
        try {
            pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(process);
            if(value instanceof Integer) {
                return ((Integer) value).longValue();
            }
        } catch (Exception e) {
            return -1L;
        }
        return -1L;
    }

    @Test
    public void test0() throws Exception{
        ProcessBuilder pb = new ProcessBuilder("echo", "hello");
        Process process = pb.start();
        long pid = getPid(process);
        process.destroy();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTrace(event("PROCESS", ProcessBuilder.class.getMethod("start"),
                annotation("process.command", "[echo, hello]"),
                annotation("process.pid", pid)));
    }

    @Test
    public void test1() throws Exception{
        Process process = Runtime.getRuntime().exec("echo", new String[]{"hello"});
        long pid = getPid(process);
        process.destroy();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event("PROCESS", ProcessBuilder.class.getMethod("start"),
                annotation("process.command", "[echo]"),
                annotation("process.pid", pid)));
    }
}
