package com.nhn.pinpoint.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.PropertyUtils;
import com.nhn.pinpoint.util.NumberUtils;

public class ProfilerConfig {

	private static final Logger logger = Logger.getLogger(ProfilerConfig.class.getName());

	private boolean profileEnable = false;

	public String collectorServerIp = "127.0.0.1";
	public int collectorServerPort = 9995;

	private boolean jdbcProfile = true;
	private boolean jdbcProfileMySql = true;
	private boolean jdbcProfileMsSql = true;
	private boolean jdbcProfileOracle = true;
	private boolean jdbcProfileCubrid = true;
	private boolean jdbcProfileDbcp = true;

    // 전역 샘플링
    private boolean samplingEnable = true;
    private int samplingRate = 1;

    // 전역 샘플링이 수행된 이후의 부분 샘플링
	private boolean samplingElapsedTimeBaseEnable;
	private int samplingElapsedTimeBaseBufferSize;
	private boolean samplingElapsedTimeBaseDiscard;
	private long samplingElapsedTimeBaseDiscardTimeLimit;


	private int profileJvmCollectInterval;
	
	private Set<String> profileInclude = new HashSet<String>();
	private Set<String> profileIncludeSub = new HashSet<String>();

    private final long DEFAULT_HEART_BEAT_INTERVAL = 5*60*1000L;
	private long heartbeatInterval = DEFAULT_HEART_BEAT_INTERVAL;

	private ServiceType serviceType = ServiceType.TOMCAT;
	
	public ProfilerConfig() {
	}

	public void readConfigFile(String pinpiontConfigFileName) throws IOException {
		try {
			Properties properties = PropertyUtils.readProperties(pinpiontConfigFileName);
			readPropertyValues(properties);
		} catch (FileNotFoundException fe) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, pinpiontConfigFileName + " file is not exists. Please check configuration.");
			}
			throw fe;
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, pinpiontConfigFileName + " file read error. Cause:" + e.getMessage(), e);
			}
			throw e;
		}
	}

	public String getCollectorServerIp() {
		return collectorServerIp;
	}

	public int getCollectorServerPort() {
		return collectorServerPort;
	}

	public boolean isProfileEnable() {
		return profileEnable;
	}

	public boolean isJdbcProfile() {
		return jdbcProfile;
	}

	public boolean isJdbcProfileMySql() {
		return jdbcProfileMySql;
	}

	public boolean isJdbcProfileMsSql() {
		return jdbcProfileMsSql;
	}

	public boolean isJdbcProfileOracle() {
		return jdbcProfileOracle;
	}

	public boolean isJdbcProfileCubrid() {
		return jdbcProfileCubrid;
	}


    public boolean isSamplingEnable() {
        return samplingEnable;
    }


    public int getSamplingRate() {
        return samplingRate;
    }

    public boolean isSamplingElapsedTimeBaseEnable() {
		return samplingElapsedTimeBaseEnable;
	}

	public int getSamplingElapsedTimeBaseBufferSize() {
		return samplingElapsedTimeBaseBufferSize;
	}

	public boolean isSamplingElapsedTimeBaseDiscard() {
		return samplingElapsedTimeBaseDiscard;
	}

	public long getSamplingElapsedTimeBaseDiscardTimeLimit() {
		return samplingElapsedTimeBaseDiscardTimeLimit;
	}

	public int getProfileJvmCollectInterval() {
		return profileJvmCollectInterval;
	}
	
	public long getHeartbeatInterval() {
		return heartbeatInterval;
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}

	private void readPropertyValues(Properties prop) {
		// TODO : use Properties defaultvalue instead of using temp variable.

		this.profileEnable = readBoolean(prop, "profile.enable", true);

		this.collectorServerIp = readString(prop, "server.collector.ip", "127.0.0.1");
		this.collectorServerPort = readInt(prop, "server.collector.udp.port", 9995);

		// JDBC
		this.jdbcProfile = readBoolean(prop, "profile.jdbc", true);
		this.jdbcProfileMySql = readBoolean(prop, "profile.jdbc.mysql", true);
		this.jdbcProfileMsSql = readBoolean(prop, "profile.jdbc.mssql", true);
		this.jdbcProfileOracle = readBoolean(prop, "profile.jdbc.oracle", true);
		this.jdbcProfileCubrid = readBoolean(prop, "profile.jdbc.cubrid", true);
		this.jdbcProfileDbcp = readBoolean(prop, "profile.jdbc.dbcp", true);


        this.samplingEnable = readBoolean(prop, "sampling.enable", true);
        this.samplingRate = readInt(prop, "sampling.rate", 1);

		// 샘플링 + io 조절 bufferSize 결정
		this.samplingElapsedTimeBaseEnable = readBoolean(prop, "sampling.elapsedtimebase.enable", true);
        // 버퍼 사이즈는 여기에 있는것은 문제가 있는것도 같음. 설정 조정의 필요성이 있음.
		this.samplingElapsedTimeBaseBufferSize = readInt(prop, "sampling.elapsedtimebase.buffersize", 20);
		this.samplingElapsedTimeBaseDiscard = readBoolean(prop, "sampling.elapsedtimebase.discard", true);
		this.samplingElapsedTimeBaseDiscardTimeLimit = readLong(prop, "sampling.elapsedtimebase.discard.timelimit", 1000);

		// JVM
		this.profileJvmCollectInterval = readInt(prop, "profile.jvm.collect.interval", 1000);

		this.heartbeatInterval = readLong(prop, "agent.heartbeat.interval", DEFAULT_HEART_BEAT_INTERVAL);
		
		// service type
		this.serviceType = readServiceType(prop, "agent.servicetype", ServiceType.TOMCAT);
		
		// profile package include
		// TODO 제거, 서비스 적용에 call stack view가 잘 보이는지 테스트하려고 추가함.
		// 수집 데이터 크기 문제로 실 서비스에서는 사용 안함.
		// 나중에 필요에 따라 정규식으로 바꿔도 되고...
		String profileableClass = readString(prop, "profile.include", "");
        setProfilableClass(profileableClass);

        logger.info("configuration loaded successfully.");
	}

    public void setProfilableClass(String profilableClass) {
        if (profilableClass == null || profilableClass.length() == 0) {
            return;
        }
        String[] className = profilableClass.split(",");
        for (String str : className) {
            if (str.endsWith(".*")) {
                this.profileIncludeSub.add(str.substring(0, str.length() - 2).replace('.', '/') + "/");
            } else {
                String replace = str.trim().replace('.', '/');
                this.profileInclude.add(replace);
            }
        }
    }

    private String readString(Properties prop, String propertyName, String defaultValue) {
		String value = prop.getProperty(propertyName, defaultValue);
		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + value);
		}
		return value;
	}

	private int readInt(Properties prop, String propertyName, int defaultValue) {
		String value = prop.getProperty(propertyName);
		int result = NumberUtils.parseInteger(value, defaultValue);
		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + result);
		}
		return result;
	}

	private long readLong(Properties prop, String propertyName, long defaultValue) {
		String value = prop.getProperty(propertyName);
		long result = NumberUtils.parseLong(value, defaultValue);
		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + result);
		}
		return result;
	}
	
	private ServiceType readServiceType(Properties prop, String propertyName, ServiceType defaultValue) {
		String value = prop.getProperty(propertyName);
		ServiceType svcType;

		if (value == null) {
			svcType = defaultValue;
		} else {
			svcType = ServiceType.valueOf(value);
		}

		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + svcType);
		}

		return svcType;
	}

	private boolean readBoolean(Properties prop, String propertyName, boolean defaultValue) {
		String value = prop.getProperty(propertyName, Boolean.toString(defaultValue));
		boolean result = Boolean.parseBoolean(value);
		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + result);
		}
		return result;
	}

	public boolean isJdbcProfileDbcp() {
		return jdbcProfileDbcp;
	}
	
	/**
	 * TODO remove this. 테스트 장비에서 call stack view가 잘 보이는지 테스트 하려고 추가함.
	 * 
	 * @param className
	 * @return
	 */
	public boolean isProfilableClass(String className) {
		if (profileInclude.contains(className)) {
			return true;
		} else {
			String packageName = className.substring(0, className.lastIndexOf("/") + 1);
			for (String pkg : profileIncludeSub) {
				if (packageName.startsWith(pkg)) {
					return true;
				}
			}
		}
		return false;
	}

    @Override
    public String toString() {
        return "ProfilerConfig{" +
                "\n profileEnable=" + profileEnable +
                "\n collectorServerIp='" + collectorServerIp + '\'' +
                "\n collectorServerPort=" + collectorServerPort +
                "\n jdbcProfile=" + jdbcProfile +
                "\n jdbcProfileMySql=" + jdbcProfileMySql +
                "\n jdbcProfileMsSql=" + jdbcProfileMsSql +
                "\n jdbcProfileOracle=" + jdbcProfileOracle +
                "\n jdbcProfileCubrid=" + jdbcProfileCubrid +
                "\n jdbcProfileDbcp=" + jdbcProfileDbcp +
                "\n samplingEnable=" + samplingEnable +
                "\n samplingRate=" + samplingRate +
                "\n samplingElapsedTimeBaseEnable=" + samplingElapsedTimeBaseEnable +
                "\n samplingElapsedTimeBaseBufferSize=" + samplingElapsedTimeBaseBufferSize +
                "\n samplingElapsedTimeBaseDiscard=" + samplingElapsedTimeBaseDiscard +
                "\n samplingElapsedTimeBaseDiscardTimeLimit=" + samplingElapsedTimeBaseDiscardTimeLimit +
                "\n profileJvmCollectInterval=" + profileJvmCollectInterval +
                "\n profileInclude=" + profileInclude +
                "\n profileIncludeSub=" + profileIncludeSub +
                "\n heartbeatInterval=" + heartbeatInterval +
                "\n serviceType=" + serviceType +
                '}';
    }
}
