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

    /**
     * Agent를 수동으로 startup해야하는가 여부.
     * Application에 별도 acceptor가 없어서 startup함수를 agent 초기화 할 때 해주어야하는 경우.
     * 예를 들어 서비스타입이 BLOC, STAND_ALONE인 경우.
     */
    private boolean manuallyStartupRequired = false;
    
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
    
    public boolean isManuallyStartupRequired() {
		return manuallyStartupRequired;
	}

	public boolean resolve() {
        String applicationHome = System.getProperty("catalina.home");

        if (applicationHome == null) {
        	// FIXME BLOC_HOME, BLOC_BASE가 있을 듯.
        	applicationHome = System.getProperty("user.dir");
        }

        if (logger.isInfoEnabled()) {
        	logger.info("Resolved ApplicationHome:{}", applicationHome);
        }

        /**
         * application type이 설정파일에 지정되어있는 경우.
         */
        if (defaultType != null) {
            logger.info("Configured applicationServerType:{}", defaultType);
            return initializeApplicationInfo(applicationHome, defaultType);
        }

		/**
		 * application type이 설정파일에 없으면 자동 검색
		 */
		final File bloc3CatalinaJar = new File(applicationHome + "/server/lib/catalina.jar");
		final File bloc3ServletApiJar = new File(applicationHome + "/common/lib/servlet-api.jar");
		final File bloc4LibDir = new File(applicationHome + "/libs");

		if (isFileExist(bloc3CatalinaJar) && isFileExist(bloc3ServletApiJar)) {
			this.manuallyStartupRequired = false;
			return initializeApplicationInfo(applicationHome, ServiceType.BLOC);
		} else if (isFileExist(bloc4LibDir)) {
			this.manuallyStartupRequired = true;
			return initializeApplicationInfo(applicationHome, ServiceType.BLOC);
		} else if (isFileExist(new File(applicationHome + "/lib/catalina.jar"))) {
			this.manuallyStartupRequired = false;
			return initializeApplicationInfo(applicationHome, ServiceType.TOMCAT);
		} else {
			this.manuallyStartupRequired = true;
			return initializeApplicationInfo(applicationHome, ServiceType.STAND_ALONE);
		}
	}

	private boolean initializeApplicationInfo(String applicationHome, ServiceType serviceType) {
		if (applicationHome == null) {
			logger.warn("applicationHome is null");
			return false;
		}

		if (ServiceType.TOMCAT.equals(serviceType)) {
			this.serverLibPath = new String[] { applicationHome + "/lib/servlet-api.jar", applicationHome + "/lib/catalina.jar" };
		} else if (ServiceType.BLOC.equals(serviceType)) {
			// FIXME serverLibPath지정 방법을 개선할 수 있을 듯.
			if (manuallyStartupRequired) {
				// BLOC 4.x
				this.serverLibPath = new String[] {};
			} else {
				// BLOC 3.x
				this.serverLibPath = new String[] { applicationHome + "/server/lib/catalina.jar", applicationHome + "/common/lib/servlet-api.jar" };
			}
		} else if (ServiceType.STAND_ALONE.equals(serviceType)) {
			this.serverLibPath = new String[] {};
		} else if (ServiceType.TEST_STAND_ALONE.equals(serviceType)) {
			this.serverLibPath = new String[] {};
		} else {
			logger.warn("Invalid Default ApplicationServiceType:{} ", defaultType);
			return false;
		}

		this.serverType = serviceType;

		logger.info("ApplicationServerType:{}, RequiredServerLibraryPath:{}", serverType, serverLibPath);

		return true;
	}

    private boolean isFileExist(File libFile) {
        final boolean found = libFile.exists(); // && libFile.isFile();
        if (found) {
            logger.debug("libFile found:{}", libFile);
        } else {
            logger.debug("libFile not found:{}", libFile);
        }
        return found;
    }
}
