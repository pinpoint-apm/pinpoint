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

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirectory;
import com.navercorp.pinpoint.bootstrap.agentdir.LogDirCleaner;
import com.navercorp.pinpoint.bootstrap.banner.PinpointBannerImpl;
import com.navercorp.pinpoint.bootstrap.classloader.PinpointClassLoaderFactory;
import com.navercorp.pinpoint.bootstrap.classloader.ProfilerLibs;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.config.PropertyLoader;
import com.navercorp.pinpoint.bootstrap.config.PropertyLoaderFactory;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.banner.PinpointBanner;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.util.OsEnvSimpleProperty;
import com.navercorp.pinpoint.common.util.PropertySnapshot;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.SystemProperty;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * @author Jongho Moon
 */
class PinpointStarter {

    private final BootLogger logger = BootLogger.getLogger(getClass());

    public static final String AGENT_TYPE = "AGENT_TYPE";

    public static final String DEFAULT_AGENT = "DEFAULT_AGENT";
    public static final String BOOT_CLASS = "com.navercorp.pinpoint.profiler.DefaultAgent";

    public static final String PLUGIN_TEST_AGENT = "PLUGIN_TEST";
    public static final String PLUGIN_TEST_BOOT_CLASS = "com.navercorp.pinpoint.profiler.test.PluginTestAgent";

    private final SimpleProperty systemProperty = SystemProperty.INSTANCE;

    private final Map<String, String> agentArgs;
    private final AgentType agentType;
    private final AgentDirectory agentDirectory;
    private final Instrumentation instrumentation;
    private final ClassLoader parentClassLoader;
    private final ModuleBootLoader moduleBootLoader;


    public PinpointStarter(ClassLoader parentClassLoader, Map<String, String> agentArgs,
                           AgentDirectory agentDirectory,
                           Instrumentation instrumentation, ModuleBootLoader moduleBootLoader) {
        //        null == BootstrapClassLoader
//        if (bootstrapClassLoader == null) {
//            throw new NullPointerException("bootstrapClassLoader");
//        }
        this.agentArgs = Objects.requireNonNull(agentArgs, "agentArgs");
        this.agentType = getAgentType(agentArgs);
        this.parentClassLoader = parentClassLoader;
        this.agentDirectory = Objects.requireNonNull(agentDirectory, "agentDirectory");
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.moduleBootLoader = moduleBootLoader;

    }

    private AgentType getAgentType(Map<String, String> agentArgs) {
        final String agentTypeParameter = agentArgs.get(AgentParameter.AGENT_TYPE);
        return AgentType.getAgentType(agentTypeParameter);
    }

    boolean start() {
        final AgentIds agentIds = resolveAgentIds();
        if (agentIds == null) {
            logger.warn("Failed to resolve AgentId and ApplicationId");
            return false;
        }

        final AgentId agentId = AgentId.random();

        final String applicationName = agentIds.getApplicationName();
        if (applicationName == null) {
            logger.warn("applicationName is null");
            return false;
        }
        final String serviceName = agentIds.getServiceName();
        if (serviceName == null) {
            logger.warn("serviceName is null");
            return false;
        }

        final ContainerResolver containerResolver = new ContainerResolver();
        final boolean isContainer = containerResolver.isContainer();

        try {
            final Properties properties = loadProperties();

            ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
            if (!profilerConfig.getPinpointDisable().equalsIgnoreCase("false")) {
                this.logger.warn("value of disable property is not false, pinpoint.disable=" + profilerConfig.getPinpointDisable());
                return false;
            }

            // set the path of log file as a system property
            saveLogFilePath(agentDirectory.getAgentLogFilePath());
            savePinpointVersion();

            cleanLogDir(agentDirectory.getAgentLogFilePath(), profilerConfig);

            // this is the library list that must be loaded
            URL[] urls = resolveLib(agentDirectory);
            List<String> agentClassloaderLibs = getAgentClassloaderLibs(profilerConfig);
            final ClassLoader agentClassLoader = createClassLoader("pinpoint.agent", urls, parentClassLoader, agentClassloaderLibs);
            if (moduleBootLoader != null) {
                this.logger.info("defineAgentModule");
                moduleBootLoader.defineAgentModule(agentClassLoader, urls);
            }

            final String bootClass = getBootClass();
            AgentBootLoader agentBootLoader = new AgentBootLoader(bootClass, agentClassLoader);
            logger.info(String.format("pinpoint agent [%s] starting...", bootClass));

            final List<Path> pluginJars = agentDirectory.getPlugins();
            final String agentName = agentIds.getAgentName();
            AgentOption option = createAgentOption(agentId, agentName, applicationName, serviceName, isContainer,
                    profilerConfig,
                    instrumentation,
                    pluginJars,
                    agentDirectory.getBootDir().getJarPath());
            Agent pinpointAgent = agentBootLoader.boot(option);
            pinpointAgent.start();
            pinpointAgent.registerStopHandler();

            logger.info("pinpoint agent started normally.");

            final PinpointBannerImpl banner = new PinpointBannerImpl(profilerConfig.readList("pinpoint.banner.configs"), logger);
            banner.setPinpointBannerMode(PinpointBanner.Mode.valueOf(profilerConfig.readString("pinpoint.banner.mode", "CONSOLE").toUpperCase()));
            banner.setPinpointBannerProperty(properties);
            banner.printBanner();
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.warn(ProductInfo.NAME + " start failed.", e);
            return false;
        }
        return true;
    }

    private List<String> getAgentClassloaderLibs(ProfilerConfig profilerConfig) {
        Set<String> libs = new HashSet<>();
        libs.addAll(ProfilerLibs.PINPOINT_PROFILER_CLASS);
        libs.addAll(profilerConfig.getAgentClassloaderAdditionalLibs());

        List<String> copy = new ArrayList<>(libs);
        copy.sort(String.CASE_INSENSITIVE_ORDER);
        return copy;
    }

    private void cleanLogDir(Path agentLogFilePath, ProfilerConfig config) {
        final int logDirMaxBackupSize = config.getLogDirMaxBackupSize();
        logger.info("Log directory maxbackupsize=" + logDirMaxBackupSize);
        LogDirCleaner logDirCleaner = new LogDirCleaner(agentLogFilePath, logDirMaxBackupSize);
        logDirCleaner.clean();
    }

    private AgentIds resolveAgentIds() {
        AgentIdResolverBuilder builder = new AgentIdResolverBuilder();
        builder.addAgentArgument(agentArgs);
        builder.addEnvProperties(System.getenv());
        builder.addSystemProperties(System.getProperties());
        AgentIdResolver agentIdResolver = builder.build();
        return agentIdResolver.resolve();
    }

    private Properties loadProperties() {

        final Path agentDirPath = agentDirectory.getAgentDirPath();
        final Path profilesPath = agentDirectory.getProfilesPath();
        final String[] profileDirs = agentDirectory.getProfileDirs();

        final SimpleProperty javaSystemProperty = copyJavaSystemProperty();
        final SimpleProperty osEnvProperty = copyOSEnvVariables();

        final PropertyLoaderFactory factory = new PropertyLoaderFactory(javaSystemProperty, osEnvProperty,
                agentDirPath, profilesPath, profileDirs);
        final PropertyLoader loader = factory.newPropertyLoader();
        final Properties properties = loader.load();

        if (this.agentType == AgentType.PLUGIN_TEST) {
            properties.put(DefaultProfilerConfig.PROFILER_INTERCEPTOR_EXCEPTION_PROPAGATE, "true");
        }
        final String importPluginIds = StringUtils.defaultString(this.agentArgs.get(AgentParameter.IMPORT_PLUGIN), "");
        properties.put(DefaultProfilerConfig.IMPORT_PLUGIN, importPluginIds);

        return properties;
    }

    private SimpleProperty copyJavaSystemProperty() {
        return new PropertySnapshot(System.getProperties());
    }

    private SimpleProperty copyOSEnvVariables() {
        return new OsEnvSimpleProperty(System.getenv());
    }


    private ClassLoader createClassLoader(final String name, final URL[] urls, final ClassLoader parentClassLoader, List<String> libClass) {
        return PinpointClassLoaderFactory.createClassLoader(name, urls, parentClassLoader, libClass);
    }

    private String getBootClass() {
        if (isTestAgent()) {
            return PLUGIN_TEST_BOOT_CLASS;
        }
        return BOOT_CLASS;
    }

    private boolean isTestAgent() {
        final String agentType = getAgentType();
        return PLUGIN_TEST_AGENT.equalsIgnoreCase(agentType);
    }

    private String getAgentType() {
        String agentType = agentArgs.get(AGENT_TYPE);
        if (agentType == null) {
            return DEFAULT_AGENT;
        }
        return agentType;

    }

    private AgentOption createAgentOption(AgentId agentId, String agentName, String applicationName, String serviceName,
                                          boolean isContainer,
                                          ProfilerConfig profilerConfig,
                                          Instrumentation instrumentation,
                                          List<Path> pluginJars,
                                          List<Path> bootstrapJarPaths) {
        List<String> pluginJarStrPath = toPathList(pluginJars);
        List<String> bootstrapJarPathStrPath = toPathList(bootstrapJarPaths);
        return new DefaultAgentOption(instrumentation, agentId, agentName, applicationName, serviceName,
                isContainer, profilerConfig, pluginJarStrPath, bootstrapJarPathStrPath);
    }

    private List<String> toPathList(List<Path> paths) {
        List<String> list = new ArrayList<>(paths.size());
        for (Path path : paths) {
            list.add(path.toString());
        }
        return list;
    }

    private void saveLogFilePath(Path agentLogFilePath) {
        logger.info("logPath:" + agentLogFilePath);
        systemProperty.setProperty(ProductInfo.NAME + ".log", agentLogFilePath.toString());
    }

    private void savePinpointVersion() {
        logger.info(String.format("pinpoint version:%s", Version.VERSION));
        systemProperty.setProperty(ProductInfo.NAME + ".version", Version.VERSION);
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
        if (DEFAULT_AGENT.equalsIgnoreCase(getAgentType())) {
            final List<Path> releaseLib = filterTest(urlList);
            return order(releaseLib);
        } else {
            logger.info("load " + PLUGIN_TEST_AGENT + " lib");
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
