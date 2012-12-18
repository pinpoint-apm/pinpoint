package com.profiler.config;

import com.profiler.common.util.PropertyUtils;
import com.profiler.util.NumberUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ProfilerConfig {


    private static final Logger logger = Logger.getLogger(ProfilerConfig.class.getName());

    private boolean profileEnable = true;

    @Deprecated
    public static String SERVER_IP = "127.0.0.1";
    @Deprecated
    public static int SERVER_UDP_PORT = 9995;

    public String collectorServerIp = "127.0.0.1";
    public int collectorServerPort = 9995;

    @Deprecated
    public static int AGENT_TCP_LISTEN_PORT = 9990;
    @Deprecated
    public static int SERVER_TCP_LISTEN_PORT = 9991;


    public static long JVM_STAT_GAP = 5000L;
    public static long SERVER_CONNECT_RETRY_GAP = 1000L;

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

    private void readPropertyValues(Properties prop) {
        // TODO : use Properties defaultvalue instead of using temp variable.

        this.profileEnable = readBoolean(prop, "PROFILE_ENABLE", true);

        this.collectorServerIp = readString(prop, "SERVER_IP", "127.0.0.1");
        this.collectorServerPort = readInt(prop, "SERVER_UDP_PORT", 9995);

        String temp = null;
        if ((temp = prop.getProperty("AGENT_TCP_LISTEN_PORT")) != null) {
            this.AGENT_TCP_LISTEN_PORT = Integer.parseInt(temp);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("AGENT_TCP_LISTEN_PORT=" + AGENT_TCP_LISTEN_PORT);
            }
        }
        if ((temp = prop.getProperty("SERVER_TCP_LISTEN_PORT")) != null) {
            this.SERVER_TCP_LISTEN_PORT = Integer.parseInt(temp);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("SERVER_TCP_LISTEN_PORT=" + SERVER_TCP_LISTEN_PORT);
            }
        }
        if ((temp = prop.getProperty("JVM_STAT_GAP")) != null) {
            this.JVM_STAT_GAP = Long.parseLong(temp);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("JVM_STAT_GAP=" + JVM_STAT_GAP);
            }
        }
        if ((temp = prop.getProperty("SERVER_CONNECT_RETRY_GAP")) != null) {
            this.SERVER_CONNECT_RETRY_GAP = Long.parseLong(temp);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("SERVER_CONNECT_RETRY_GAP=" + SERVER_CONNECT_RETRY_GAP);
            }
        }

        // JDBC
        this.jdbcProfile = readBoolean(prop, "JDBC_PROFILE", true);
        this.jdbcProfileMySql = readBoolean(prop, "JDBC_PROFILE_MYSQL", true);
        this.jdbcProfileMsSql = readBoolean(prop, "JDBC_PROFILE_MSSQL", true);
        this.jdbcProfileOracle = readBoolean(prop, "JDBC_PROFILE_ORACLE", true);
        this.jdbcProfileCubrid = readBoolean(prop, "JDBC_PROFILE_CUBRID", true);
        this.jdbcProfileDbcp = readBoolean(prop, "JDBC_PROFILE_DBCP", true);

        // 샘플링 + io 조절 bufferSize 결정
        this.samplingElapsedTimeBaseEnable = readBoolean(prop, "sampling.elapsedtimebase.enable", true);
        this.samplingElapsedTimeBaseBufferSize = readInt(prop, "sampling.elapsedtimebase.buffersize", 20);
        this.samplingElapsedTimeBaseDiscard = readBoolean(prop, "sampling.elapsedtimebase.discard", true);
        this.samplingElapsedTimeBaseDiscardTimeLimit = readLong(prop, "sampling.elapsedtimebase.discard.timelimit", 1000);

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

    private long readLong(Properties prop, String propertyName, int defaultValue) {
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

    @Override
    public String toString() {
        return "ProfilerConfig{" +
                "profileEnable=" + profileEnable +
                ", jdbcProfile=" + jdbcProfile +
                ", jdbcProfileMySql=" + jdbcProfileMySql +
                ", jdbcProfileMsSql=" + jdbcProfileMsSql +
                ", jdbcProfileOracle=" + jdbcProfileOracle +
                ", jdbcProfileCubrid=" + jdbcProfileCubrid +
                ", jdbcProfileDbcp=" + jdbcProfileDbcp +
                '}';
    }
}
