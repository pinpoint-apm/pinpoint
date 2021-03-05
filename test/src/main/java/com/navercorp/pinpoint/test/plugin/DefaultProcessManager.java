/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.PINPOINT_TEST_ID;
import static com.navercorp.pinpoint.test.plugin.util.SystemPropertyBuilder.format;

/**
 * @author Taejin Koo
 */
public class DefaultProcessManager implements ProcessManager {

    private final TaggedLogger logger = TestLogger.getLogger();

    private final PluginTestContext context;
    private Process process = null;

    public DefaultProcessManager(PluginTestContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public Process create(PinpointPluginTestInstance pluginTestInstance) {
        if (process == null) {
            ProcessBuilder builder = new ProcessBuilder();

            builder.command(buildCommand(pluginTestInstance));
            builder.redirectErrorStream(true);
            builder.directory(pluginTestInstance.getWorkingDirectory());
            logger.info("Working directory: {}", System.getProperty("user.dir"));
            logger.info("Command: {}", builder.command());
            try {
                this.process = builder.start();
            } catch (IOException e) {
                logger.error(e, "process start failed");
            }
        } else {
            throw new IllegalStateException("Already create Process");
        }

        return process;
    }

    @Override
    public Process get() {
        return process;
    }

    @Override
    public void stop() {
        if (process != null) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    process.destroy();
                }

            }, 10 * 1000);

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn(e, "waitFor() is interrupted");
            }

            timer.cancel();
            process = null;
        }
    }

    private String[] buildCommand(PinpointPluginTestInstance pluginTestInstance) {
        List<String> list = new ArrayList<String>();

        list.add(context.getJavaExecutable());

        list.add("-cp");
        list.add(getClassPathAsString(pluginTestInstance));

        list.add(getAgent());

        list.add(format("pinpoint.agentId", "build.test.0"));
        list.add(format("pinpoint.applicationName", "test"));
        list.add(format(PINPOINT_TEST_ID, pluginTestInstance.getTestId()));

        list.addAll(Arrays.asList(context.getJvmArguments()));

        if (context.isDebug()) {
            list.addAll(getDebugOptions());
        }

        if (context.getProfile() != null) {
            list.add(format("pinpoint.profiler.profiles.active", context.getProfile()));
        }

        if (context.getConfigFile() != null) {
            list.add(format("pinpoint.config", context.getConfigFile()));
            list.add(format("pinpoint.config.load.mode", "simple"));
        }

        list.addAll(pluginTestInstance.getVmArgs());

        String mainClass = pluginTestInstance.getMainClass();

        if (mainClass.endsWith(".jar")) {
            list.add("-jar");
        }

        list.add(mainClass);
        list.addAll(pluginTestInstance.getAppArgs());

        return list.toArray(new String[0]);
    }

    private List<String> getDebugOptions() {
        return Arrays.asList("-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=1296,server=y,suspend=y");
    }

    private String getAgent() {
        return String.format("-javaagent:%s=AGENT_TYPE=PLUGIN_TEST", context.getAgentJar());
    }

    private String getClassPathAsString(PinpointPluginTestInstance pluginTestInstance) {
        StringBuilder classPath = new StringBuilder();
        boolean first = true;

        for (String lib : pluginTestInstance.getClassPath()) {
            if (first) {
                first = false;
            } else {
                classPath.append(File.pathSeparatorChar);
            }

            classPath.append(lib);
        }

        return classPath.toString();
    }

}
