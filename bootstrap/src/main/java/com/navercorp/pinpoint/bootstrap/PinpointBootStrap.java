/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.util.IdValidateUtils;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.TypeProviderLoader;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemProperty;

/**
 * @author emeroad
 * @author netspider
 */
public class PinpointBootStrap {

    private static final Logger logger = Logger.getLogger(PinpointBootStrap.class.getName());

    public static final String BOOT_CLASS = "com.navercorp.pinpoint.profiler.DefaultAgent";

    private static final boolean STATE_NONE = false;
    private static final boolean STATE_STARTED = true;
    private static final AtomicBoolean LOAD_STATE = new AtomicBoolean(STATE_NONE);

    private static final SimpleProperty SYSTEM_PROPERTY = SystemProperty.INSTANCE;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.CAMEL_NAME + " agentArgs:" + agentArgs);
        }
        
        Map<String, String> argMap = parseAgentArgs(agentArgs);
        
        final boolean duplicated = checkDuplicateLoadState();
        if (duplicated) {
            logPinpointAgentLoadFail();
            return;
        }
        // 1st find boot-strap.jar
        final ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            logger.severe("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar Fnot found.");
            logPinpointAgentLoadFail();
            return;
        }
        // 2st find boot-strap-core.jar
        final String bootStrapCoreJar = classPathResolver.getBootStrapCoreJar();
        if (bootStrapCoreJar == null) {
            logger.severe("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
            logPinpointAgentLoadFail();
            return;
        }
        JarFile bootStrapCoreJarFile = getBootStrapJarFile(bootStrapCoreJar);
        if (bootStrapCoreJarFile == null) {
            logger.severe("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
            logPinpointAgentLoadFail();
            return;
        }
        logger.info("load pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar :" + bootStrapCoreJar);
        instrumentation.appendToBootstrapClassLoaderSearch(bootStrapCoreJarFile);

        if (!isValidId("pinpoint.agentId", PinpointConstants.AGENT_NAME_MAX_LEN)) {
            logPinpointAgentLoadFail();
            return;
        }
        if (!isValidId("pinpoint.applicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN)) {
            logPinpointAgentLoadFail();
            return;
        }
        
        URL[] pluginJars = classPathResolver.resolvePlugins();
        loadServiceTypeProviders(pluginJars);

        String configPath = getConfigPath(classPathResolver);
        if (configPath == null) {
            logPinpointAgentLoadFail();
            return;
        }

        // set the path of log file as a system property
        saveLogFilePath(classPathResolver);

        try {
            // Is it right to load the configuration in the bootstrap?
            ProfilerConfig profilerConfig = ProfilerConfig.load(configPath);

            // this is the library list that must be loaded
            List<URL> libUrlList = resolveLib(classPathResolver);
            AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
            String bootClass = argMap.containsKey("bootClass") ? argMap.get("bootClass") : BOOT_CLASS;
            agentClassLoader.setBootClass(bootClass);
            logger.info("pinpoint agent [" + bootClass + "] starting...");
            agentClassLoader.boot(agentArgs, instrumentation, profilerConfig, pluginJars);
            logger.info("pinpoint agent started normally.");
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.log(Level.SEVERE, ProductInfo.CAMEL_NAME + " start failed. Error:" + e.getMessage(), e);
            logPinpointAgentLoadFail();
        }

    }
    
    private static Map<String, String> parseAgentArgs(String str) {
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
        
        return map;
    }
    
    private static void loadServiceTypeProviders(URL[] pluginJars) {
        TypeProviderLoader.initializeServiceType(pluginJars);
    }

    private static JarFile getBootStrapJarFile(String bootStrapCoreJar) {
        try {
            return new JarFile(bootStrapCoreJar);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, bootStrapCoreJar + " file not found.", ioe);
            return null;
        }
    }

    private static void logPinpointAgentLoadFail() {
        final String errorLog =
            "*****************************************************************************\n" +
            "* Pinpoint Agent load failure\n" +
            "*****************************************************************************";
        System.err.println(errorLog);
    }

    // for test
    static boolean getLoadState() {
        return LOAD_STATE.get();
    }

    private static boolean checkDuplicateLoadState() {
        final boolean startSuccess = LOAD_STATE.compareAndSet(STATE_NONE, STATE_STARTED);
        if (startSuccess) {
            return false;
        } else {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("pinpoint-bootstrap already started. skipping agent loading.");
            }
            return true;
        }
    }
    
    private static boolean isValidId(String propertyName, int maxSize) {
        logger.info("check -D" + propertyName);
        String value = SYSTEM_PROPERTY.getProperty(propertyName);
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

    private static int getLength(String value) {
        final byte[] bytes = BytesUtils.toBytes(value);
        if (bytes == null) {
            return 0;
        } else {
            return bytes.length;
        }
    }


    private static void saveLogFilePath(ClassPathResolver classPathResolver) {
        String agentLogFilePath = classPathResolver.getAgentLogFilePath();
        logger.info("logPath:" + agentLogFilePath);

        SYSTEM_PROPERTY.setProperty(ProductInfo.NAME + ".log", agentLogFilePath);
    }

    private static String getConfigPath(ClassPathResolver classPathResolver) {
        final String configName = ProductInfo.NAME + ".config";
        String pinpointConfigFormSystemProperty = SYSTEM_PROPERTY.getProperty(configName);
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


    private static List<URL> resolveLib(ClassPathResolver classPathResolver)  {
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
