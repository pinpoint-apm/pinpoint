/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.trace;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.FileUtils;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.loader.plugins.PinpointPluginLoader;
import com.navercorp.pinpoint.loader.plugins.trace.TraceMetadataProviderLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataVerificationTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointPluginLoader<TraceMetadataProvider> traceMetadataProviderLoader = new TraceMetadataProviderLoader();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void checkTraceMetadata() throws IOException {

        URL[] libArray = getClassPath();
        ClassLoader classLoader = new URLClassLoader(libArray);


        List<TraceMetadataProvider> traceMetadataProviders = traceMetadataProviderLoader.load(classLoader);
        TraceMetadataVerifier verifier = new TraceMetadataVerifier();
        for (TraceMetadataProvider traceMetadataProvider : traceMetadataProviders) {
            traceMetadataProvider.setup(verifier.getTraceMetadataSetupContext());
        }
        verifier.verifyServiceTypes(collector);
        verifier.verifyAnnotationKeys(collector);
    }

    private URL[] getClassPath() throws IOException {
        String classPaths = System.getProperty("java.class.path");
//        logger.debug("classPath:{}", classPaths);

        List<String> classPathList = StringUtils.tokenizeToStringList(classPaths, ";");
        List<String> libs = filterJdk6Plugin(classPathList);
        List<URL> urls = toURls(libs);
        return urls.toArray(new URL[0]);
    }

    private List<URL> toURls(List<String> libs) throws IOException {
        List<URL> list = new ArrayList<URL>();
        for (String lib : libs) {
            list.add(FileUtils.toURL(lib));
        }
        return list;
    }

    private List<String> filterJdk6Plugin(List<String> classPathList) {
        List<String> list = new ArrayList<String>();
        JvmVersion currentJvmVersion = JvmUtils.getVersion();
        for (String jarPath : classPathList) {
            // name filter
            if (!jarPath.endsWith(String.format("-plugin-%s.jar", Version.VERSION))) {
                list.add(jarPath);
                continue;
            }

            logger.info("plugin:{}", jarPath);
            PluginJar pluginJar = PluginJar.fromFilePath(jarPath);
            JvmVersion pluginVersion = JvmVersion.getFromVersion(pluginJar.getPluginCompilerVersion());
            if (currentJvmVersion.getClassVersion() >= pluginVersion.getClassVersion()) {
                list.add(jarPath);
                logger.info("currentJvm:{} pluginVersion:{}", currentJvmVersion, pluginVersion);
            }
        }

        return list;
    }
}
