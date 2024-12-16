/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirectory;
import com.navercorp.pinpoint.bootstrap.config.BootStrapOptions;
import com.navercorp.pinpoint.bootstrap.config.OsEnvSimpleProperty;
import com.navercorp.pinpoint.bootstrap.config.PropertyLoader;
import com.navercorp.pinpoint.bootstrap.config.PropertyLoaderFactory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Jongho Moon
 */
class PinpointStarter {

    private final BootLogger logger = BootLogger.getLogger(getClass());

    private final Instrumentation instrumentation;
    private final ClassLoader parentClassLoader;

    private final Map<String, String> agentArgs;
    private final AgentType agentType;
    private final AgentDirectory agentDirectory;


    public PinpointStarter(Instrumentation instrumentation,
                           ClassLoader parentClassLoader,
                           Map<String, String> agentArgs,
                           AgentDirectory agentDirectory) {
        //        null == BootstrapClassLoader
//        if (bootstrapClassLoader == null) {
//            throw new NullPointerException("bootstrapClassLoader");
//        }
        this.agentArgs = Objects.requireNonNull(agentArgs, "agentArgs");
        this.agentType = AgentType.of(agentArgs::get);
        this.parentClassLoader = parentClassLoader;
        this.agentDirectory = Objects.requireNonNull(agentDirectory, "agentDirectory");
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
    }

    private ModuleBootLoader loadModuleBootLoader(Instrumentation instrumentation, ClassLoader parentClassLoader) {
        if (!ModuleUtils.isModuleSupported()) {
            logger.info("no java-module detected");
            return null;
        }
        BootLogger moduleLogger = BootLogger.getLogger(ModuleBootLoader.class);
        moduleLogger.info("java-module detected");
        moduleLogger.info("ModuleBootLoader start");

        ModuleBootLoader moduleBootLoader = new ModuleBootLoader(instrumentation, parentClassLoader, moduleLogger::info);
        moduleBootLoader.loadModuleSupport();
        return moduleBootLoader;
    }



    boolean start() {

        try {
            final Properties properties = loadProperties();

            BootStrapOptions bootStrapOptions = new BootStrapOptions(properties);
            if (bootStrapOptions.getPinpointDisable()) {
                this.logger.warn("value of disable property is not false, pinpoint.disable=" + bootStrapOptions.getPinpointDisable());
                return false;
            }

            // this is the library list that must be loaded
            URL[] urls = resolveLib(agentDirectory);
            final ClassLoader agentClassLoader = createClassLoader("pinpoint.agent", urls, parentClassLoader, properties);
            final ModuleBootLoader moduleBootLoader = loadModuleBootLoader(instrumentation, parentClassLoader);
            if (moduleBootLoader != null) {
                this.logger.info("defineAgentModule");
                moduleBootLoader.defineAgentModule(agentClassLoader, urls);
            }

            AgentBootLoader agentBootLoader = new AgentBootLoader(agentType.getClassName(), agentClassLoader);
            logger.info(String.format("pinpoint agent [%s] starting...", agentType));

            final List<Path> pluginJars = agentDirectory.getPlugins();
            AgentOption option = createAgentOption(
                    instrumentation,
                    properties,
                    agentArgs,
                    agentDirectory.getAgentDirPath(),
                    pluginJars,
                    agentDirectory.getBootDir().getJarPath());
            Object agent = agentBootLoader.boot(option);
            Class<?> agentClazz = agent.getClass();
            agentClazz.getMethod("start").invoke(agent);
            agentClazz.getMethod("registerStopHandler").invoke(agent);
            logger.info("pinpoint agent started normally.");
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.warn("pinpoint start failed.", e);
            return false;
        }
        return true;
    }

    // TestAgent only
    public static final String IMPORT_PLUGIN = "profiler.plugin.import-plugin";


    private Properties loadProperties() {

        final Path agentDirPath = agentDirectory.getAgentDirPath();
        final Path profilesPath = agentDirectory.getProfilesPath();
        final List<Path> profileDirs = agentDirectory.getProfileDirs();

        final Properties javaSystemProperty = copyJavaSystemProperty();
        final Properties osEnvProperty = copyOSEnvVariables();

        final PropertyLoaderFactory factory = new PropertyLoaderFactory(javaSystemProperty, osEnvProperty,
                agentDirPath, profilesPath, profileDirs);
        final PropertyLoader loader = factory.newPropertyLoader();
        final Properties properties = loader.load();

        final String importPluginIds = this.agentArgs.getOrDefault(AgentParameter.IMPORT_PLUGIN, "");
        properties.put(IMPORT_PLUGIN, importPluginIds);

        return properties;
    }

    private Properties copyJavaSystemProperty() {
        return new Properties(System.getProperties());
    }

    private Properties copyOSEnvVariables() {
        return new OsEnvSimpleProperty().toProperties(System.getenv());
    }

    private ClassLoader createClassLoader(final String name, final URL[] urls, final ClassLoader parentClassLoader, Properties properties) {
        try {
            Class<?> classLoaderFactory = Class.forName("com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory");
            Method method = classLoaderFactory.getMethod("createClassLoader", String.class, URL[].class, ClassLoader.class, Properties.class);
            return (ClassLoader) method.invoke(classLoaderFactory, name, urls, parentClassLoader, properties);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }


    private AgentOption createAgentOption(Instrumentation instrumentation,
                                          Properties profilerConfig,
                                          Map<String, String> agentArgs,

                                          Path agentPath,
                                          List<Path> pluginJars,
                                          List<Path> bootstrapJarPaths) {
        return new DefaultAgentOption(instrumentation, profilerConfig, agentArgs, agentPath, pluginJars, bootstrapJarPaths);
    }

    private URL[] resolveLib(AgentDirectory classPathResolver) {
        // this method may handle only absolute path,  need to handle relative path (./..agentlib/lib)
        Path agentJarFullPath = classPathResolver.getAgentJarFullPath();
        Path agentLibPath = classPathResolver.getAgentLibPath();
        List<Path> libUrlList = resolveLib(classPathResolver.getLibs());
        Path agentConfigPath = classPathResolver.getAgentConfigPath();

        if (logger.isInfoEnabled()) {
            logger.info(String.format("agent JarPath:%s", agentJarFullPath));
            logger.info(String.format("agent LibDir:%s", agentLibPath));
            for (Path url : libUrlList) {
                logger.info(String.format("agent Lib:%s", url));
            }
            logger.info(String.format("agent config:%s", agentConfigPath));
        }
        return PathUtils.toURLs(libUrlList);
    }

    private static final String PINPOINT_PREFIX = "pinpoint-";

    private List<Path> resolveLib(List<Path> urlList) {
        if (agentType == AgentType.DEFAULT_AGENT) {
            final List<Path> releaseLib = filterTest(urlList);
            return order(releaseLib);
        } else {
            logger.info("load " + AgentType.PLUGIN_TEST + " lib");
            // plugin test
            return order(urlList);
        }
    }

    private List<Path> order(List<Path> releaseLib) {
        final List<Path> orderList = new ArrayList<>(releaseLib.size());
        // pinpoint module first
        for (Path path : releaseLib) {
            Path fileName = path.getFileName();
            if (fileName != null) {
                if (fileName.startsWith(PINPOINT_PREFIX)) {
                    orderList.add(path);
                }
            }
        }
        for (Path path : releaseLib) {
            Path fileName = path.getFileName();
            if (fileName != null) {
                if (!fileName.startsWith(PINPOINT_PREFIX)) {
                    orderList.add(path);
                }
            }
        }
        return orderList;
    }

    private List<Path> filterTest(List<Path> pathList) {
        final List<Path> releaseLib = new ArrayList<>(pathList.size());
        for (Path path : pathList) {
            if (!path.toString().contains("pinpoint-profiler-test")) {
                releaseLib.add(path);
            }
        }
        return releaseLib;
    }

}
