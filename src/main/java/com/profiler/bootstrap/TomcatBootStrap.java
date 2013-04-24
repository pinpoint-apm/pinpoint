package com.profiler.bootstrap;

import com.profiler.ProductInfo;
import com.profiler.config.ProfilerConfig;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class TomcatBootStrap {

    private static final Logger logger = Logger.getLogger(TomcatBootStrap.class.getName());



    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (agentArgs != null) {
            logger.info(ProductInfo.CAMEL_NAME + " agentArgs:" + agentArgs);
        }

        ClassPathResolver classPathResolver = new ClassPathResolver();
        boolean agentJarNotFound = classPathResolver.findAgentJar();
        if (!agentJarNotFound) {
            // TODO 이거 변경해야 함.
            logger.severe("hippo-profiler-bootstrap-x.x.x.jar not found.");
            return;
        }

        String configPath = getConfigPath(classPathResolver);
        if(configPath == null ) {
            // 설정파일을 못찾으므로 종료.
            return;
        }
        // 로그가 저장될 위치를 시스템 properties로 저장한다.
        saveLogFilePath(classPathResolver);


        try {
            // 설정파일 로드 이게 bootstrap에 있어야 되나는게 맞나?
            ProfilerConfig profilerConfig = new ProfilerConfig();
            profilerConfig.readConfigFile(configPath);

            // 이게 로드할 lib List임.
            List<URL> libUrlList = resolveLib(classPathResolver);
            AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
            agentClassLoader.setBootClass("com.profiler.DefaultAgent");
            agentClassLoader.boot(agentArgs, instrumentation, profilerConfig);

        } catch (Exception e) {
            logger.log(Level.SEVERE, ProductInfo.CAMEL_NAME + " start fail. Caused:" + e.getMessage(), e);
        }

    }

    private static void saveLogFilePath(ClassPathResolver classPathResolver) {
        String agentLogFilePath = classPathResolver.getAgentLogFilePath();
        logger.info("logPath:" + agentLogFilePath);

        System.setProperty(ProductInfo.NAME + "." + "log", agentLogFilePath);
    }

    private static String getConfigPath(ClassPathResolver classPathResolver) {
        final String configName = ProductInfo.NAME + ".config";
        String hippoConfigFormSystemProperty = System.getProperty(configName);
        if (hippoConfigFormSystemProperty != null) {
            logger.info(configName + " systemProperty found. " + hippoConfigFormSystemProperty);
            return hippoConfigFormSystemProperty;
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
        // 절대경로만 처리되지 않나함. 상대 경로(./../agentlib/lib등)일 경우의 처리가 있어야 될것 같음.
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
