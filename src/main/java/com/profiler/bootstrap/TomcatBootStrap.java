package com.profiler.bootstrap;

import com.profiler.ProductInfo;
import com.profiler.config.ProfilerConfig;
import com.profiler.logging.LoggerBinder;
import com.profiler.logging.LoggerFactory;

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
            logger.info(ProductInfo.NAME_CAMEL + " agentArgs:" + agentArgs);
        }
        if (logger.isLoggable(Level.FINE)) {
            dumpSystemProperties();
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


        // 이게 로드할 lib List임.

        try {
            // 설정파일 로드 이게 bootstrap에 있어야 되나는게 맞나?
            ProfilerConfig profilerConfig = new ProfilerConfig();
            profilerConfig.readConfigFile(configPath);


            List<URL> libUrlList = resolveLib(classPathResolver);
            AgentClassLoader agentClassLoader = new AgentClassLoader(libUrlList.toArray(new URL[libUrlList.size()]));
            agentClassLoader.setBootClass("com.profiler.DefaultAgent");
            agentClassLoader.boot(agentArgs, instrumentation, profilerConfig);

        } catch (Exception e) {
            logger.log(Level.SEVERE, ProductInfo.NAME_CAMEL + " start fail. Caused:" + e.getMessage(), e);
        }

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
        logger.info("agentJarPath:" + agentJarFullPath);

        String agentLibPath = classPathResolver.getAgentLibPath();
        logger.info("agentLibPath:" + agentLibPath);

        List<URL> urlList = classPathResolver.resolveLib();
        logger.info("agent lib list:" + urlList);

        String agentConfigPath = classPathResolver.getAgentConfigPath();
        logger.info("agent config:" + agentConfigPath);

        return urlList;
    }

    private static void dumpSystemProperties() {
        if (logger.isLoggable(Level.FINE)) {
            Properties properties = System.getProperties();
            Set<String> strings = properties.stringPropertyNames();
            for (String key : strings) {
                logger.fine("SystemProperties " + key + "=" + properties.get(key));
            }
        }
    }

}
