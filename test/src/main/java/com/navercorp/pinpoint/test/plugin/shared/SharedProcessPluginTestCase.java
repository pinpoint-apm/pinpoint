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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.test.plugin.ForkedPinpointPluginTest;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestContext;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.navercorp.pinpoint.test.plugin.PinpointPluginTestConstants.CHILD_CLASS_PATH_PREFIX;

/**
 * @author Taejin Koo
 */
public class SharedProcessPluginTestCase implements PinpointPluginTestInstance {

    private static final String DEFAULT_ENCODING = Charsets.UTF_8_NAME;

    private final PinpointPluginTestContext context;
    private final String testId;
    private final List<String> libs;
    private final boolean onSystemClassLoader;
    private final SharedProcessManager processManager;

    public SharedProcessPluginTestCase(PinpointPluginTestContext context, String testId, List<String> libs, boolean onSystemClassLoader, SharedProcessManager processManager) {
        this.context = context;
        this.testId = testId + ":" + (onSystemClassLoader ? "system" : "child") + ":" + context.getJvmVersion();
        this.libs = libs;
        this.onSystemClassLoader = onSystemClassLoader;
        this.processManager = Assert.requireNonNull(processManager, "processManager");
    }

    @Override
    public String getTestId() {
        return testId;
    }

    @Override
    public List<String> getClassPath() {
        if (onSystemClassLoader) {
            List<String> libs = new ArrayList<String>(context.getRequiredLibraries());
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
        return ForkedPinpointPluginTest.class.getName();
//            return ForkedPinpointPluginTestV2.class.getName();
    }

    @Override
    public List<String> getAppArgs() {
        List<String> args = new ArrayList<String>();

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
        Process process = processManager.create(this);
        InputStream inputStream = process.getInputStream();
        return new Scanner(inputStream, DEFAULT_ENCODING);
    }

    @Override
    public void endTest() throws Exception {
        try {
            processManager.deregisterTest(testId);
        } finally {
            processManager.stop();
        }
        // do nothing
    }

    @Override
    public File getWorkingDirectory() {
        return new File(".");
    }

    @Override
    public String toString() {
        return "ShareProcessPluginTestCase{" +
                "testId='" + testId + '\'' +
                '}';
    }

}
