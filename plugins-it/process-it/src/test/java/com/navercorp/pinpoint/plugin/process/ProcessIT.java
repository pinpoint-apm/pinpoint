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

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.OsType;
import com.navercorp.pinpoint.common.util.OsUtils;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PinpointAgent(AgentPath.PATH)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-process-plugin"})
@Dependency({PluginITConstants.VERSION})
@PinpointConfig("pinpoint-process-test.config")
public class ProcessIT {
    private static final String UNIX_PROCESS = "java.lang.UNIXProcess";

    private static final String[] CMD = newCommandLine();

    private static final long PID_NOT_FOUND = -1;

    private static String[] newCommandLine() {
        OsType type = OsUtils.getType();
        if (OsType.WINDOW == type) {
            return new String[] {"CMD", "/C"};
        }
        return new String[] {"echo", "hello"};
    }

    private long getPid(Process process) {
        if (!process.getClass().getName().equals(UNIX_PROCESS)) {
            return -1;
        }
        Field pidField = null;
        try {
            pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(process);
            if (value instanceof Integer) {
                return ((Integer) value).longValue();
            }
        } catch (Exception e) {
            return PID_NOT_FOUND;
        }
        return PID_NOT_FOUND;
    }

    private ExpectedAnnotation[] processAnnotations(long pid) {
        List<ExpectedAnnotation> list = new ArrayList<>();

        ExpectedAnnotation processCommand = new ExpectedAnnotation("process.command", Arrays.toString(CMD));
        list.add(processCommand);

        if (pid != PID_NOT_FOUND) {
            ExpectedAnnotation processPid = new ExpectedAnnotation("process.pid", pid);
            list.add(processPid);
        }
        return list.toArray(new ExpectedAnnotation[0]);
    }

    @Test
    public void test0() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(CMD);
        Process process = pb.start();
        long pid = getPid(process);
        process.destroy();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        ExpectedTrace event = event("PROCESS", ProcessBuilder.class.getMethod("start"),
                processAnnotations(pid)
        );
        verifier.verifyTrace(event);
    }

    @Test
    public void test1() throws Exception {
        Process process = Runtime.getRuntime().exec(CMD);
        long pid = getPid(process);
        process.destroy();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        ExpectedTrace event = event("PROCESS", ProcessBuilder.class.getMethod("start"),
                processAnnotations(pid));
        verifier.verifyTrace(event);
    }
}
