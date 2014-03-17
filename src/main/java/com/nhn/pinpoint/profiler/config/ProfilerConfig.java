package com.nhn.pinpoint.profiler.config;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.util.NumberUtils;

/**
 * @author emeroad
 */
public class ProfilerConfig {

	private static final Logger logger = Logger.getLogger(ProfilerConfig.class.getName());

	private boolean profileEnable = false;

	public String collectorServerIp = "127.0.0.1";
    public int collectorUdpSpanServerPort = 9996;
    public int collectorUdpServerPort = 9995;
    public int collectorTcpServerPort = 9994;

    private int spanDataSenderWriteQueueSize = 128;
    private int statDataSenderWriteQueueSize = 128;

    private int jdbcSqlCacheSize = 1024;
	private boolean jdbcProfile = true;

	private boolean jdbcProfileMySql = true;
    private boolean jdbcProfileMySqlSetAutoCommit = false;
    private boolean jdbcProfileMySqlCommit = false;
    private boolean jdbcProfileMySqlRollback = false;

	private boolean jdbcProfileMsSql = true;

	private boolean jdbcProfileOracle = true;
    private boolean jdbcProfileOracleSetAutoCommit = false;
    private boolean jdbcProfileOracleCommit = false;
    private boolean jdbcProfileOracleRollback = false;

	private boolean jdbcProfileCubrid = true;
    private boolean jdbcProfileCubridSetAutoCommit = false;
    private boolean jdbcProfileCubridCommit = false;
    private boolean jdbcProfileCubridRollback = false;

	private boolean jdbcProfileDbcp = true;
    private boolean jdbcProfileDbcpConnectionClose = false;

    private boolean arucs = true;
    private boolean arucsKeyTrace = false;
    private boolean memcached = true;
    private boolean memcachedKeyTrace = false;


    private boolean apacheHttpClient4Profile = true;
    private boolean apacheHttpClient4ProfileCookie = false;
    private DumpType apacheHttpClient4ProfileCookieDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient4ProfileCookieSamplingRate = 1;
    private boolean apacheHttpClient4ProfileEntity = false;
    private DumpType apacheHttpClient4ProfileEntityDumpType = DumpType.EXCEPTION;
    private int apacheHttpClient4ProfileEntitySamplingRate = 1;


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

	private ServiceType applicationServerType;

    public ProfilerConfig() {
	}

	public void readConfigFile(String pinpiontConfigFileName) throws IOException {
		try {
			Properties properties = readProperties(pinpiontConfigFileName);
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

    private Properties readProperties(String configFileName) throws IOException {
        if (configFileName == null) {
            throw new NullPointerException("configFileName must not be null");
        }
        Properties properties = new Properties();
        InputStream in = null;
        Reader reader = null;
        try {
            in = new FileInputStream(configFileName);
            reader = new InputStreamReader(in, "UTF-8");
            properties.load(reader);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
        return properties;
    }

    public String getCollectorServerIp() {
		return collectorServerIp;
	}

    public int getCollectorUdpSpanServerPort() {
        return collectorUdpSpanServerPort;
    }

    public int getCollectorUdpServerPort() {
		return collectorUdpServerPort;
	}

    public int getCollectorTcpServerPort() {
        return collectorTcpServerPort;
    }

    public int getStatDataSenderWriteQueueSize() {
        return statDataSenderWriteQueueSize;
    }

    public int getSpanDataSenderWriteQueueSize() {
        return spanDataSenderWriteQueueSize;
    }

    public boolean isProfileEnable() {
		return profileEnable;
	}

	public boolean isJdbcProfile() {
		return jdbcProfile;
	}

    public int getJdbcSqlCacheSize() {
        return jdbcSqlCacheSize;
    }

    // mysql start -----------------------------------------------------
	public boolean isJdbcProfileMySql() {
		return jdbcProfileMySql;
	}

    public boolean isJdbcProfileMySqlSetAutoCommit() {
        return jdbcProfileMySqlSetAutoCommit;
    }

    public boolean isJdbcProfileMySqlCommit() {
        return jdbcProfileMySqlCommit;
    }

    public boolean isJdbcProfileMySqlRollback() {
        return jdbcProfileMySqlRollback;
    }
    // mysql end-----------------------------------------------------

    public boolean isJdbcProfileMsSql() {
		return jdbcProfileMsSql;
	}

    // oracle start -----------------------------------------------------
	public boolean isJdbcProfileOracle() {
		return jdbcProfileOracle;
	}

    public boolean isJdbcProfileOracleSetAutoCommit() {
        return jdbcProfileOracleSetAutoCommit;
    }

    public boolean isJdbcProfileOracleCommit() {
        return jdbcProfileOracleCommit;
    }

    public boolean isJdbcProfileOracleRollback() {
        return jdbcProfileOracleRollback;
    }
    // oracle end -----------------------------------------------------

    // cubrid start -----------------------------------------------------
    public boolean isJdbcProfileCubrid() {
		return jdbcProfileCubrid;
	}

    public boolean isJdbcProfileCubridSetAutoCommit() {
        return jdbcProfileCubridSetAutoCommit;
    }

    public boolean isJdbcProfileCubridCommit() {
        return jdbcProfileCubridCommit;
    }

    public boolean isJdbcProfileCubridRollback() {
        return jdbcProfileCubridRollback;
    }
    // cubrid end -----------------------------------------------------

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

    public boolean isJdbcProfileDbcp() {
        return jdbcProfileDbcp;
    }

    public boolean isJdbcProfileDbcpConnectionClose() {
        return jdbcProfileDbcpConnectionClose;
    }

    public boolean isArucs() {
        return arucs;
    }

    public boolean isArucsKeyTrace() {
        return arucsKeyTrace;
    }

    public boolean isMemcached() {
        return memcached;
    }

    public boolean isMemcachedKeyTrace() {
        return memcachedKeyTrace;
    }

    //-----------------------------------------
    // http apache client

    public boolean isApacheHttpClient4Profile() {
        return apacheHttpClient4Profile;
    }

    public boolean isApacheHttpClient4ProfileCookie() {
        return apacheHttpClient4ProfileCookie;
    }

    public DumpType getApacheHttpClient4ProfileCookieDumpType() {
        return apacheHttpClient4ProfileCookieDumpType;
    }

    public int getApacheHttpClient4ProfileCookieSamplingRate() {
        return apacheHttpClient4ProfileCookieSamplingRate;
    }

    public boolean isApacheHttpClient4ProfileEntity() {
        return apacheHttpClient4ProfileEntity;
    }

    public DumpType getApacheHttpClient4ProfileEntityDumpType() {
        return apacheHttpClient4ProfileEntityDumpType;
    }

    public int getApacheHttpClient4ProfileEntitySamplingRate() {
        return apacheHttpClient4ProfileEntitySamplingRate;
    }

    //-----------------------------------------

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
	
	public ServiceType getApplicationServerType() {
		return applicationServerType;
	}

    public void setApplicationServerType(ServiceType applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    private void readPropertyValues(Properties prop) {
		// TODO : use Properties defaultvalue instead of using temp variable.

		this.profileEnable = readBoolean(prop, "profiler.enable", true);

		this.collectorServerIp = readString(prop, "profiler.collector.ip", "127.0.0.1");
        this.collectorUdpSpanServerPort = readInt(prop, "profiler.collector.udpspan.port", 9996);
		this.collectorUdpServerPort = readInt(prop, "profiler.collector.udp.port", 9995);
        this.collectorTcpServerPort = readInt(prop, "profiler.collector.tcp.port", 9994);

        this.spanDataSenderWriteQueueSize = readInt(prop, "profiler.spandatasender.write.queue.size", 128);
        this.statDataSenderWriteQueueSize = readInt(prop, "profiler.statdatasender.write.queue.size", 128);


		// JDBC
		this.jdbcProfile = readBoolean(prop, "profiler.jdbc", true);

        this.jdbcSqlCacheSize = readInt(prop, "profiler.jdbc.sqlcachesize", 1024);

		this.jdbcProfileMySql = readBoolean(prop, "profiler.jdbc.mysql", true);
        this.jdbcProfileMySqlSetAutoCommit = readBoolean(prop, "profiler.jdbc.mysql.setautocommit", false);
        this.jdbcProfileMySqlCommit = readBoolean(prop, "profiler.jdbc.mysql.commit", false);
        this.jdbcProfileMySqlRollback = readBoolean(prop, "profiler.jdbc.mysql.rollback", false);


		this.jdbcProfileMsSql = readBoolean(prop, "profiler.jdbc.mssql", true);


		this.jdbcProfileOracle = readBoolean(prop, "profiler.jdbc.oracle", true);
        this.jdbcProfileOracleSetAutoCommit = readBoolean(prop, "profiler.jdbc.oracle.setautocommit", false);
        this.jdbcProfileOracleCommit = readBoolean(prop, "profiler.jdbc.oracle.commit", false);
        this.jdbcProfileOracleRollback = readBoolean(prop, "profiler.jdbc.oracle.rollback", false);


		this.jdbcProfileCubrid = readBoolean(prop, "profiler.jdbc.cubrid", true);
        this.jdbcProfileCubridSetAutoCommit = readBoolean(prop, "profiler.jdbc.cubrid.setautocommit", false);
        this.jdbcProfileCubridCommit = readBoolean(prop, "profiler.jdbc.cubrid.commit", false);
        this.jdbcProfileCubridRollback = readBoolean(prop, "profiler.jdbc.cubrid.rollback", false);


		this.jdbcProfileDbcp = readBoolean(prop, "profiler.jdbc.dbcp", true);
        this.jdbcProfileDbcpConnectionClose = readBoolean(prop, "profiler.jdbc.dbcp.connectionclose", false);

        this.arucs = readBoolean(prop, "profiler.arcus", true);
        this.arucsKeyTrace = readBoolean(prop, "profiler.arcus.keytrace", false);
        this.memcached = readBoolean(prop, "profiler.memcached", true);
        this.memcachedKeyTrace = readBoolean(prop, "profiler.memcached.keytrace", false);


        this.apacheHttpClient4Profile = readBoolean(prop, "profiler.apache.httpclient4", true);
        this.apacheHttpClient4ProfileCookie = readBoolean(prop, "profiler.apache.httpclient4.cookie", false);
        this.apacheHttpClient4ProfileCookieDumpType = readDumpType(prop, "profiler.apache.httpclient4.cookie.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient4ProfileCookieSamplingRate = readInt(prop, "profiler.apache.httpclient4.cookie.sampling.rate", 1);

        this.apacheHttpClient4ProfileEntity = readBoolean(prop, "profiler.apache.httpclient4.entity", false);
        this.apacheHttpClient4ProfileEntityDumpType = readDumpType(prop, "profiler.apache.httpclient4.entity.dumptype", DumpType.EXCEPTION);
        this.apacheHttpClient4ProfileEntitySamplingRate = readInt(prop, "profiler.apache.httpclient4.entity.sampling.rate", 1);


        this.samplingEnable = readBoolean(prop, "profiler.sampling.enable", true);
        this.samplingRate = readInt(prop, "profiler.sampling.rate", 1);

		// 샘플링 + io 조절 bufferSize 결정
		this.samplingElapsedTimeBaseEnable = readBoolean(prop, "profiler.sampling.elapsedtimebase.enable", true);
        // 버퍼 사이즈는 여기에 있는것은 문제가 있는것도 같음. 설정 조정의 필요성이 있음.
		this.samplingElapsedTimeBaseBufferSize = readInt(prop, "profiler.sampling.elapsedtimebase.buffersize", 20);
		this.samplingElapsedTimeBaseDiscard = readBoolean(prop, "profiler.sampling.elapsedtimebase.discard", true);
		this.samplingElapsedTimeBaseDiscardTimeLimit = readLong(prop, "profiler.sampling.elapsedtimebase.discard.timelimit", 1000);

		// JVM
		this.profileJvmCollectInterval = readInt(prop, "profiler.jvm.collect.interval", 1000);

		this.heartbeatInterval = readLong(prop, "profiler.heartbeat.interval", DEFAULT_HEART_BEAT_INTERVAL);
		
		// service type
		this.applicationServerType = readServiceType(prop, "profiler.applicationservertype");
		
		// profile package include
		// TODO 제거, 서비스 적용에 call stack view가 잘 보이는지 테스트하려고 추가함.
		// 수집 데이터 크기 문제로 실 서비스에서는 사용 안함.
		// 나중에 필요에 따라 정규식으로 바꿔도 되고...
		String profileableClass = readString(prop, "profiler.include", "");
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

    private DumpType readDumpType(Properties prop, String propertyName, DumpType defaultDump) {
        String propertyValue = prop.getProperty(propertyName);
        if (propertyValue == null) {
            propertyValue = defaultDump.name();
        }
        String value = propertyValue.toUpperCase();
        DumpType result;
        try {
            result = DumpType.valueOf(value);
        } catch (IllegalArgumentException e) {
            result = defaultDump;
        }
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
	
	private ServiceType readServiceType(Properties prop, String propertyName) {
		String value = prop.getProperty(propertyName);
        if (value == null) {
            return null;
        }
		ServiceType serviceType = getServiceType(value);
		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + serviceType);
		}
		return serviceType;
	}

    private ServiceType getServiceType(String defaultValue) {
        try {
            return ServiceType.valueOf(defaultValue);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "ServiceType.valueOf() fail. " + defaultValue + " Caused:" + e.getMessage(), e);
            return null;
        }
    }

    private boolean readBoolean(Properties prop, String propertyName, boolean defaultValue) {
		String value = prop.getProperty(propertyName, Boolean.toString(defaultValue));
		boolean result = Boolean.parseBoolean(value);
		if (logger.isLoggable(Level.INFO)) {
			logger.info(propertyName + "=" + result);
		}
		return result;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(512);
        sb.append("ProfilerConfig{");
        sb.append("\n profileEnable=").append(profileEnable);
        sb.append("\n collectorServerIp='").append(collectorServerIp).append('\'');
        sb.append("\n collectorUdpSpanServerPort=").append(collectorUdpSpanServerPort);
        sb.append("\n collectorUdpServerPort=").append(collectorUdpServerPort);
        sb.append("\n collectorTcpServerPort=").append(collectorTcpServerPort);
        sb.append("\n jdbcSqlCacheSize=").append(jdbcSqlCacheSize);
        sb.append("\n jdbcProfile=").append(jdbcProfile);
        sb.append("\n jdbcProfileMySql=").append(jdbcProfileMySql);
        sb.append("\n jdbcProfileMySqlSetAutoCommit=").append(jdbcProfileMySqlSetAutoCommit);
        sb.append("\n jdbcProfileMySqlCommit=").append(jdbcProfileMySqlCommit);
        sb.append("\n jdbcProfileMySqlRollback=").append(jdbcProfileMySqlRollback);
        sb.append("\n jdbcProfileMsSql=").append(jdbcProfileMsSql);
        sb.append("\n jdbcProfileOracle=").append(jdbcProfileOracle);
        sb.append("\n jdbcProfileOracleSetAutoCommit=").append(jdbcProfileOracleSetAutoCommit);
        sb.append("\n jdbcProfileOracleCommit=").append(jdbcProfileOracleCommit);
        sb.append("\n jdbcProfileOracleRollback=").append(jdbcProfileOracleRollback);
        sb.append("\n jdbcProfileCubrid=").append(jdbcProfileCubrid);
        sb.append("\n jdbcProfileCubridSetAutoCommit=").append(jdbcProfileCubridSetAutoCommit);
        sb.append("\n jdbcProfileCubridCommit=").append(jdbcProfileCubridCommit);
        sb.append("\n jdbcProfileCubridRollback=").append(jdbcProfileCubridRollback);
        sb.append("\n jdbcProfileDbcp=").append(jdbcProfileDbcp);
        sb.append("\n jdbcProfileDbcpConnectionClose=").append(jdbcProfileDbcpConnectionClose);
        sb.append("\n arucs=").append(arucs);
        sb.append("\n arucsKeyTrace=").append(arucsKeyTrace);
        sb.append("\n memcached=").append(memcached);
        sb.append("\n memcachedKeyTrace=").append(memcachedKeyTrace);
        sb.append("\n samplingEnable=").append(samplingEnable);
        sb.append("\n samplingRate=").append(samplingRate);
        sb.append("\n samplingElapsedTimeBaseEnable=").append(samplingElapsedTimeBaseEnable);
        sb.append("\n samplingElapsedTimeBaseBufferSize=").append(samplingElapsedTimeBaseBufferSize);
        sb.append("\n samplingElapsedTimeBaseDiscard=").append(samplingElapsedTimeBaseDiscard);
        sb.append("\n samplingElapsedTimeBaseDiscardTimeLimit=").append(samplingElapsedTimeBaseDiscardTimeLimit);
        sb.append("\n profileJvmCollectInterval=").append(profileJvmCollectInterval);
        sb.append("\n profileInclude=").append(profileInclude);
        sb.append("\n profileIncludeSub=").append(profileIncludeSub);
        sb.append("\n heartbeatInterval=").append(heartbeatInterval);
        sb.append("\n applicationServerType=").append(applicationServerType);
        sb.append('}');
        return sb.toString();
    }
}
