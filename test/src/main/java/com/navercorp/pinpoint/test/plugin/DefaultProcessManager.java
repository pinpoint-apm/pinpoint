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

import com.navercorp.pinpoint.bootstrap.config.Profiles;
import com.navercorp.pinpoint.test.plugin.util.CommandLineOption;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.PINPOINT_TEST_ID;


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
            timer.purge();
            process = null;
        }
    }

    private List<String> buildCommand(PinpointPluginTestInstance pluginTestInstance) {
        CommandLineOption option = new CommandLineOption();

        option.addOption(context.getJavaExecutable());

        option.addOption("-cp");
        option.addOption(getClassPathAsString(pluginTestInstance));

        option.addOption(getAgent());

        option.addSystemProperty("pinpoint.agentId", "build.test.0");
        option.addSystemProperty("pinpoint.applicationName", "test");
        option.addSystemProperty(PINPOINT_TEST_ID, pluginTestInstance.getTestId());

        option.addOptions(context.getJvmArguments());

        if (context.isDebug()) {
            option.addOptions(getDebugOptions());
        }

        if (context.getProfile() != null) {
            option.addSystemProperty("pinpoint.profiler.profiles.active", context.getProfile());
        }

        if (context.getConfigFile() != null) {
            option.addSystemProperty("pinpoint.config", context.getConfigFile());
            option.addSystemProperty("pinpoint.config.load.mode", "simple");
        }

        String logLocationConfig = context.getLogLocationConfig();
        if (logLocationConfig != null) {
            if (logLocationConfig.endsWith("/")) {
                option.addSystemProperty(Profiles.LOG_CONFIG_LOCATION_KEY, context.getLogLocationConfig());
            } else {
                option.addSystemProperty(Profiles.LOG_CONFIG_LOCATION_KEY, context.getLogLocationConfig() + '/');
            }
        }

        option.addOptions(pluginTestInstance.getVmArgs());

        String mainClass = pluginTestInstance.getMainClass();

        if (mainClass.endsWith(".jar")) {
            option.addOption("-jar");
        }

        option.addOption(mainClass);
        option.addOptions(pluginTestInstance.getAppArgs());

        return option.build();
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
