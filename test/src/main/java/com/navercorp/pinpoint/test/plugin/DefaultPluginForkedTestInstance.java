/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.junit5.launcher.SharedPluginForkedTestLauncher;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.CHILD_CLASS_PATH_PREFIX;

public class DefaultPluginForkedTestInstance implements PluginForkedTestInstance {
    private static final String DEFAULT_ENCODING = PluginTestConstants.UTF_8_NAME;

    private final PluginTestContext context;
    private final String testId;
    private final List<String> libs;
    private final boolean onSystemClassLoader;
    private final ProcessManager processManager;

    public DefaultPluginForkedTestInstance(PluginTestContext context, String testId, List<String> libs, boolean onSystemClassLoader) {
        this.context = context;
        this.testId = testId + ":" + (onSystemClassLoader ? "system" : "child") + ":" + context.getJvmVersion();
        this.libs = libs;
        this.onSystemClassLoader = onSystemClassLoader;
        this.processManager = new DefaultProcessManager(context);
    }

    @Override
    public String getTestId() {
        return testId;
    }

    @Override
    public List<String> getClassPath() {
        if (onSystemClassLoader) {
            List<String> libs = new ArrayList<>(context.getRequiredLibraries());
            libs.addAll(this.libs);
            libs.add(context.getTestClassLocation());

            return libs;
        } else {
            return context.getRequiredLibraries();
        }
    }

    @Override
    public List<String> getVmArgs() {
        return Arrays.asList("-Dfile.encoding=" + DEFAULT_ENCODING);
    }

    @Override
    public String getMainClass() {
        return SharedPluginForkedTestLauncher.class.getName();
    }

    @Override
    public List<String> getAppArgs() {
        List<String> args = new ArrayList<>();

        args.add(context.getTestClass().getName());

        if (!onSystemClassLoader) {
            StringBuilder classPath = new StringBuilder();
            classPath.append(CHILD_CLASS_PATH_PREFIX);

            for (String lib : libs) {
                classPath.append(lib);
                classPath.append(File.pathSeparatorChar);
            }

            classPath.append(context.getTestClassLocation());
            args.add(classPath.toString());
        }

        return args;
    }

    @Override
    public Scanner startTest() throws Exception {
        Process process = processManager.create(null);
        InputStream inputStream = process.getInputStream();
        return new Scanner(inputStream, DEFAULT_ENCODING);
    }

//    @Override
//    public Scanner startTest(PluginForkedTestInstance pinpointPluginTestInstance) throws Throwable {
//        Process process = processManager.create(null);
//        InputStream inputStream = process.getInputStream();
//        return new Scanner(inputStream, DEFAULT_ENCODING);
//    }

    @Override
    public void endTest() throws Exception {
        processManager.stop();

        // do nothing
    }

    @Override
    public File getWorkingDirectory() {
        return new File(".");
    }

}
