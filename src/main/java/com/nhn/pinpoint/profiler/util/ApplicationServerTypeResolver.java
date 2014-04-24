package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.common.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author emeroad
 * @author netspider
 */
public class ApplicationServerTypeResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private ServiceType serverType;
    private String[] serverLibPath;

    private ServiceType defaultType;


    public ApplicationServerTypeResolver(ServiceType defaultType) {
        this.defaultType = defaultType;
    }

    public ApplicationServerTypeResolver() {
    }

    public String[] getServerLibPath() {
        return serverLibPath;
    }

    public ServiceType getServerType() {
        return serverType;
    }

    public boolean resolve() {
        // tomcat인지 확인한다.
        final String catalinaHome = System.getProperty("catalina.home");

        if (logger.isInfoEnabled()) {
            logger.info("CATALINA_HOME={}", catalinaHome);
        }

        // 강제 설정 옵션.
        if (defaultType != null) {
            logger.info("applicationServerDefaultType:{}", defaultType);
            if (defaultType == ServiceType.TOMCAT) {
                return resolveTomcat(catalinaHome);
            } else if(defaultType == ServiceType.BLOC) {
                return resolveBloc(catalinaHome);
            } else if(defaultType == ServiceType.STAND_ALONE) {
                resolveStandAlone();
                logger.info("applicationServerDefaultType:{}", defaultType);
                return true;
            } else if(defaultType == ServiceType.TEST_STAND_ALONE) {
                // testcase를 돌릴경우
                logger.info("applicationServerDefaultType:{}", defaultType);
            } else {
                logger.warn("Invalid Default ApplicationServiceType:{} ", defaultType);
                return false;
            }
        }

        // 자동 검색.
        final File blocCatalinaJar = new File(catalinaHome + "/server/lib/catalina.jar");
        final File blocServletApiJar = new File(catalinaHome + "/common/lib/servlet-api.jar");
        if (isFileExist(blocCatalinaJar) && isFileExist(blocServletApiJar)) {
            return resolveBloc(catalinaHome);
        } else if(isFileExist(new File(catalinaHome + "/lib/catalina.jar")))  {
             return resolveTomcat(catalinaHome);
        } else {
        	// logger.warn("ApplicationServerType resolve fail. type not found");
        	logger.info("applicationServerDefaultType:{}", defaultType);
            return resolveStandAlone();
        }
    }

    private boolean resolveTomcat(String catalinaHome) {
        if (catalinaHome == null) {
            logger.warn("CATALINA_HOME is null");
            return false;
        }
        this.serverType = ServiceType.TOMCAT;
        this.serverLibPath =  new String[] { catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar" };
        logger.info("ApplicationServerType:{} lib:{}", serverType, serverLibPath);
        return true;
    }

    private boolean resolveBloc(String catalinaHome) {
        if (catalinaHome == null) {
            logger.warn("CATALINA_HOME is null");
            return false;
        }
        this.serverType = ServiceType.BLOC;
        this.serverLibPath = new String[] { catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar" };
        logger.info("ApplicationServerType:{} lib:{}", serverType, serverLibPath);
        return true;
    }

    private boolean resolveStandAlone() {
        this.serverType = ServiceType.STAND_ALONE;
        this.serverLibPath = new String[] {};
        logger.info("ApplicationServerType:{} lib:{}", serverType, serverLibPath);
        return true;
    }

    private boolean isFileExist(File libFile) {
        final boolean found = libFile.exists() && libFile.isFile();
        if (found) {
            logger.debug("libFile found:{}", libFile);
        } else {
            logger.info("libFile not found:{}", libFile);
        }
        return found;
    }
}
