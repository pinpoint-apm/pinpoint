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

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.util.IdValidateUtils;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.CAMEL_NAME + " agentArgs:" + agentArgs);
        }
        final boolean duplicated = checkDuplicateLoadState();
        if (duplicated) {
            logPinpointAgentLoadFail();
            return;
        }

        final ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            logger.severe("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar not found.");
            logPinpointAgentLoadFail();
            return;
        }

        if (!isValidId("pinpoint.agentId", PinpointConstants.AGENT_NAME_MAX_LEN)) {
            logPinpointAgentLoadFail();
            return;
        }
        if (!isValidId("pinpoint.applicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN)) {
            logPinpointAgentLoadFail();
            return;
        }

        String configPath = getConfigPath(classPathResolver);
        if (configPath == null ) {
            logPinpointAgentLoadFail();
            return;
        }

        // set the path of log file as a system property
        saveLogFilePath(classPathResolver);

//      TODO append (InterceptorRegistry & Interceptor)
//      instrumentation.appendToSystemClassLoaderSearch("interceptor.jar");

        try {
            // Is it right to load the configuration in the bootstrap?
            ProfilerConfig profilerConfig = ProfilerConfig.load(configPath);

            // this is the library list that must be loaded
            List<URL> libUrlList = resolveLib(classPathResolver);
            AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
            agentClassLoader.setBootClass(BOOT_CLASS);
            logger.info("pinpoint agent starting...");
            agentClassLoader.boot(classPathResolver.getAgentDirPath(), agentArgs, instrumentation, profilerConfig);
            logger.info("pinpoint agent started normally.");
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.log(Level.SEVERE, ProductInfo.CAMEL_NAME + " start failed. Error:" + e.getMessage(), e);
            logPinpointAgentLoadFail();
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
        String value = System.getProperty(propertyName);
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

        System.setProperty(ProductInfo.NAME + ".log", agentLogFilePath);
    }

    private static String getConfigPath(ClassPathResolver classPathResolver) {
        final String configName = ProductInfo.NAME + ".config";
        String pinpointConfigFormSystemProperty = System.getProperty(configName);
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
