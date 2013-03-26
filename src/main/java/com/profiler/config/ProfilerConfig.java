package com.profiler.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.util.PropertyUtils;
import com.profiler.util.NumberUtils;

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

	private boolean samplingElapsedTimeBaseEnable;
	private int samplingElapsedTimeBaseBufferSize;
	private boolean samplingElapsedTimeBaseDiscard;
	private long samplingElapsedTimeBaseDiscardTimeLimit;

	private int profileJvmCollectInterval;
	
	private Set<String> profileInclude = new HashSet<String>(4);
	private Set<String> profileIncludeSub = new HashSet<String>(4);

	private long heartbeatInterval = 5*60*1000L;
	
	public ProfilerConfig() {
	}

	public void readConfigFile() throws IOException {
		String hippoConfigFileName = System.getProperty("hippo.config");
		if (hippoConfigFileName == null) {
			logger.info("hippo.config property is not set. Using default value:" + this);
			return;
		}

		try {
			// TODO file path를 찾는 부분을 수정해야됨 현재 제대로 안찾아짐. 설정파일이 classpath 에 걸려 있지않으므로 파일위치를 못찾음.
			Properties properties = PropertyUtils.readProperties(hippoConfigFileName);
			readPropertyValues(properties);
		} catch (FileNotFoundException fe) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, hippoConfigFileName + " file is not exists. Please check configuration.");
			}
			throw fe;
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, hippoConfigFileName + " file read error. Cause:" + e.getMessage(), e);
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

		// 샘플링 + io 조절 bufferSize 결정
		this.samplingElapsedTimeBaseEnable = readBoolean(prop, "sampling.elapsedtimebase.enable", true);
		this.samplingElapsedTimeBaseBufferSize = readInt(prop, "sampling.elapsedtimebase.buffersize", 20);
		this.samplingElapsedTimeBaseDiscard = readBoolean(prop, "sampling.elapsedtimebase.discard", true);
		this.samplingElapsedTimeBaseDiscardTimeLimit = readLong(prop, "sampling.elapsedtimebase.discard.timelimit", 1000);

		// JVM
		this.profileJvmCollectInterval = readInt(prop, "profile.jvm.collect.interval", 1000);

		this.heartbeatInterval = readLong(prop, "agent.heartbeat.interval", 60000L);
		
		// profile package include
		// TODO 제거, 서비스 적용에 call stack view가 잘 보이는지 테스트하려고 추가함.
		// 수집 데이터 크기 문제로 실 서비스에서는 사용 안함.
		// 나중에 필요에 따라 정규식으로 바꿔도 되고...
		String[] tmp = readString(prop, "profile.include", "").split(",");
		for (String str : tmp) {
			if (str.endsWith(".*")) {
				this.profileIncludeSub.add(str.substring(0, str.length() - 2).replace('.', '/') + "/");
			} else {
				this.profileInclude.add(str.trim().replace('.', '/'));
			}
		}
		
		logger.info("configuration loaded successfully.");
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
		return "ProfilerConfig{" + "profileEnable=" + profileEnable + ", jdbcProfile=" + jdbcProfile + ", jdbcProfileMySql=" + jdbcProfileMySql + ", jdbcProfileMsSql=" + jdbcProfileMsSql + ", jdbcProfileOracle=" + jdbcProfileOracle + ", jdbcProfileCubrid=" + jdbcProfileCubrid + ", jdbcProfileDbcp=" + jdbcProfileDbcp + '}';
	}
}
