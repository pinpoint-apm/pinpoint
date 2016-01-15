/**
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

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.util.IdValidateUtils;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.common.service.DefaultTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class PinpointStarter {
    private static final Logger logger = Logger.getLogger(PinpointStarter   .class.getName());

    public static final String BOOT_CLASS = "com.navercorp.pinpoint.profiler.DefaultAgent";

    private SimpleProperty systemProperty = SystemProperty.INSTANCE;
    private final String agentArgs;
    private String bootStrapCore;
    private final Map<String, String> argMap;
    private final Instrumentation instrumentation;


    public PinpointStarter(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.NAME + " agentArgs:" + agentArgs);
        }
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }

        this.agentArgs = agentArgs;
        this.argMap = parseAgentArgs(agentArgs);
        this.instrumentation = instrumentation;
    }

    public void start() {
        // 1st find boot-strap.jar
        final ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            logger.severe("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar Fnot found.");
            logPinpointAgentLoadFail();
            return;
        }
        
        // 2nd find boot-strap-core.jar
        final String bootStrapCoreJar = classPathResolver.getBootStrapCoreJar();
        if (bootStrapCoreJar == null) {
            logger.severe("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
            logPinpointAgentLoadFail();
            return;
        }
        this.bootStrapCore = bootStrapCoreJar;

        
        if (!isValidId("pinpoint.agentId", PinpointConstants.AGENT_NAME_MAX_LEN)) {
            logPinpointAgentLoadFail();
            return;
        }
        if (!isValidId("pinpoint.applicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN)) {
            logPinpointAgentLoadFail();
            return;
        }

        URL[] pluginJars = classPathResolver.resolvePlugins();
        TraceMetadataLoaderService typeLoaderService = new DefaultTraceMetadataLoaderService(pluginJars);
        ServiceTypeRegistryService serviceTypeRegistryService  = new DefaultServiceTypeRegistryService(typeLoaderService);
        AnnotationKeyRegistryService annotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(typeLoaderService);

        String configPath = getConfigPath(classPathResolver);
        if (configPath == null) {
            logPinpointAgentLoadFail();
            return;
        }

        // set the path of log file as a system property
        saveLogFilePath(classPathResolver);

        savePinpointVersion();

        try {
            // Is it right to load the configuration in the bootstrap?
            ProfilerConfig profilerConfig = DefaultProfilerConfig.load(configPath);

            // this is the library list that must be loaded
            List<URL> libUrlList = resolveLib(classPathResolver);
            AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
            String bootClass = argMap.containsKey("bootClass") ? argMap.get("bootClass") : BOOT_CLASS;
            agentClassLoader.setBootClass(bootClass);
            logger.info("pinpoint agent [" + bootClass + "] starting...");

            AgentOption option = createAgentOption(agentArgs, instrumentation, profilerConfig, pluginJars, bootStrapCore, serviceTypeRegistryService, annotationKeyRegistryService);
            Agent pinpointAgent = agentClassLoader.boot(option);
            pinpointAgent.start();
            registerShutdownHook(pinpointAgent);
            logger.info("pinpoint agent started normally.");
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.log(Level.SEVERE, ProductInfo.NAME + " start failed. Error:" + e.getMessage(), e);
            logPinpointAgentLoadFail();
        }
    }

    private AgentOption createAgentOption(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig, URL[] pluginJars, String bootStrapJarCorePath, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {

        return new DefaultAgentOption(agentArgs, instrumentation, profilerConfig, pluginJars, bootStrapJarCorePath, serviceTypeRegistryService, annotationKeyRegistryService);
    }

    // for test
    void setSystemProperty(SimpleProperty systemProperty) {
        this.systemProperty = systemProperty;
    }

    private void registerShutdownHook(final Agent pinpointAgent) {
        final Runnable stop = new Runnable() {
            @Override
            public void run() {
                pinpointAgent.stop();
            }
        };
        PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory("Pinpoint-shutdown-hook");
        Thread thread = pinpointThreadFactory.newThread(stop);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    private Map<String, String> parseAgentArgs(String str) {
        Map<String, String> map = new HashMap<String, String>();

        if (str == null || str.isEmpty()) {
            return map;
        }

        Scanner scanner = new Scanner(str);
        scanner.useDelimiter("\\s*,\\s*");

        while (scanner.hasNext()) {
            String token = scanner.next();
            int assign = token.indexOf('=');

            if (assign == -1) {
                map.put(token, "");
            } else {
                map.put(token.substring(0, assign), token.substring(assign + 1));
            }
        }
        scanner.close();
        return Collections.unmodifiableMap(map);
    }

    private void logPinpointAgentLoadFail() throws PinpointException {
        final String errorLog =
                "*****************************************************************************\n" +
                        "* Pinpoint Agent load failure\n" +
                        "*****************************************************************************";
        System.err.println(errorLog);
    }


    private boolean isValidId(String propertyName, int maxSize) {
        logger.info("check -D" + propertyName);
        String value = systemProperty.getProperty(propertyName);
        if (value == null){
            logger.severe("-D" + propertyName + " is null. value:null");
            return false;
        }
        // blanks not permitted around value
        value = value.trim();
        if (value.isEmpty()) {
            logger.severe("-D" + propertyName + " is empty. value:''");
            return false;
        }

        if (!IdValidateUtils.validateId(value, maxSize)) {
            logger.severe("invalid Id. " + propertyName + " can only contain [a-zA-Z0-9], '.', '-', '_'. maxLength:" + maxSize + " value:" + value);
            return false;
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("check success. -D" + propertyName + ":" + value + " length:" + getLength(value));
        }
        return true;
    }

    private int getLength(String value) {
        final byte[] bytes = BytesUtils.toBytes(value);
        if (bytes == null) {
            return 0;
        } else {
            return bytes.length;
        }
    }


    private void saveLogFilePath(ClassPathResolver classPathResolver) {
        String agentLogFilePath = classPathResolver.getAgentLogFilePath();
        logger.info("logPath:" + agentLogFilePath);

        systemProperty.setProperty(ProductInfo.NAME + ".log", agentLogFilePath);
    }

    private void savePinpointVersion() {
        logger.info("pinpoint version:" + Version.VERSION);
        systemProperty.setProperty(ProductInfo.NAME + ".version", Version.VERSION);
    }

    private String getConfigPath(ClassPathResolver classPathResolver) {
        final String configName = ProductInfo.NAME + ".config";
        String pinpointConfigFormSystemProperty = systemProperty.getProperty(configName);
        if (pinpointConfigFormSystemProperty != null) {
            logger.info(configName + " systemProperty found. " + pinpointConfigFormSystemProperty);
            return pinpointConfigFormSystemProperty;
        }

        String classPathAgentConfigPath = classPathResolver.getAgentConfigPath();
        if (classPathAgentConfigPath != null) {
            logger.info("classpath " + configName +  " found. " + classPathAgentConfigPath);
            return classPathAgentConfigPath;
        }

        logger.severe(configName + " file not found.");
        return null;
    }


    private List<URL> resolveLib(ClassPathResolver classPathResolver)  {
        // this method may handle only absolute path,  need to handle relative path (./..agentlib/lib)
        String agentJarFullPath = classPathResolver.getAgentJarFullPath();
        String agentLibPath = classPathResolver.getAgentLibPath();
        List<URL> urlList = classPathResolver.resolveLib();
        String agentConfigPath = classPathResolver.getAgentConfigPath();

        if (logger.isLoggable(Level.INFO)) {
            logger.info("agentJarPath:" + agentJarFullPath);
            logger.info("agentLibPath:" + agentLibPath);
            logger.info("agent lib list:" + urlList);
            logger.info("agent config:" + agentConfigPath);
        }

        return urlList;
    }

}
