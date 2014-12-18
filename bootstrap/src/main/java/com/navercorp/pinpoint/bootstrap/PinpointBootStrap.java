package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.util.IdValidateUtils;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 * @author netspider
 */
public class PinpointBootStrap {

    private static final Logger logger = Logger.getLogger(PinpointBootStrap.class.getName());

    private static final int LIMIT_LENGTH = 24;
    private static final String DELIMITER = TransactionIdUtils.TRANSACTION_ID_DELIMITER;

    public static final String BOOT_CLASS = "com.navercorp.pinpoint.profiler.DefaultAgent";

    public static final String BOOT_STRAP_LOAD_STATE = "com.navercorp.pinpoint.bootstrap.load.state";
    public static final String BOOT_STRAP_LOAD_STATE_LOADING = "LOADING";
    public static final String BOOT_STRAP_LOAD_STATE_COMPLETE = "COMPLETE";
    public static final String BOOT_STRAP_LOAD_STATE_ERROR = "ERROR";


    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.CAMEL_NAME + " agentArgs:" + agentArgs);
        }
        final boolean duplicated = checkDuplicateLoadState();
        if (duplicated) {
            // Don't handle the duplicated state. Don't use it as bellow.
            //changeLoadState(BOOT_STRAP_LOAD_STATE_ERROR);
            logPinpointAgentLoadFail();
            return;
        }

        ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            // TODO must modify this
            logger.severe("pinpoint-bootstrap-x.x.x.jar not found.");
            changeLoadState(BOOT_STRAP_LOAD_STATE_ERROR);
            logPinpointAgentLoadFail();
            return;
        }

        if (!isValidId("pinpoint.agentId", PinpointConstants.AGENT_NAME_MAX_LEN)) {
            changeLoadState(BOOT_STRAP_LOAD_STATE_ERROR);
            logPinpointAgentLoadFail();
            return;
        }
        if (!isValidId("pinpoint.applicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN)) {
            changeLoadState(BOOT_STRAP_LOAD_STATE_ERROR);
            logPinpointAgentLoadFail();
            return;
        }

        String configPath = getConfigPath(classPathResolver);
        if (configPath == null ) {
            changeLoadState(BOOT_STRAP_LOAD_STATE_ERROR);
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
            agentClassLoader.setBootClass(BOOT_CLASS);
            logger.info("pinpoint agent start.");
            agentClassLoader.boot(classPathResolver.getAgentDirPath(), agentArgs, instrumentation, profilerConfig);
            logger.info("pinpoint agent start success.");
            changeLoadState(BOOT_STRAP_LOAD_STATE_COMPLETE);
        } catch (Exception e) {
            // unexpected exception that did not be checked above
            logger.log(Level.SEVERE, ProductInfo.CAMEL_NAME + " start fail. Caused:" + e.getMessage(), e);
            changeLoadState(BOOT_STRAP_LOAD_STATE_ERROR);
            logPinpointAgentLoadFail();
        }

    }

    private static void changeLoadState(String loadState) {
        System.setProperty(BOOT_STRAP_LOAD_STATE, loadState);
    }

    private static void logPinpointAgentLoadFail() {
        final String errorLog =
            "*****************************************************************************\n" +
            "* PinpointAgent load fail\n" +
            "*****************************************************************************";
        System.err.println(errorLog);
    }

    private static boolean checkDuplicateLoadState() {
        final String exist = System.getProperty(BOOT_STRAP_LOAD_STATE);
        if (exist == null) {
            changeLoadState(BOOT_STRAP_LOAD_STATE_LOADING);
        } else {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.severe("pinpoint-bootstrap already started. skip agent loading. loadState:" + exist);
            }
            return true;
        }
        return false;
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
