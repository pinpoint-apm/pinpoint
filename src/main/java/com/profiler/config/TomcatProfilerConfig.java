package com.profiler.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TomcatProfilerConfig {


    private static final Logger logger = Logger.getLogger(TomcatProfilerConfig.class.getName());

    public static String SERVER_IP = "127.0.0.1";

    // TODO 앞으로 공통으로 사용할 port가 필요함. 근데 tcp, udp 혼용사용하면 따로 port를 열어야 되는거 아닌지.
    // 일단 9995로 하자.
    public static int DEFUALT_PORT = 9995;
    public static int AGENT_TCP_LISTEN_PORT = 9990;
    public static int SERVER_TCP_LISTEN_PORT = 9991;
    public static int REQUEST_TRANSACTION_DATA_LISTEN_PORT = 9995;
    public static int REQUEST_DATA_LISTEN_PORT = 9996;
    public static int JVM_DATA_LISTEN_PORT = 9997;

    public static long JVM_STAT_GAP = 5000L;
    public static long SERVER_CONNECT_RETRY_GAP = 1000L;

    /**
     * If sql query count is over 10000 it consumes Memory. So sqlHashSet uses
     * CopyOnWriteArraySet. It is slow, but it is stable. Default set is false
     * and it uses HashSet.
     */
    public static boolean QUERY_COUNT_OVER_10000 = false;

    private boolean JDBC_PROFILE = true;

    public static TomcatProfilerConfig readConfigFile() {
        TomcatProfilerConfig config = new TomcatProfilerConfig();

        String hippoConfigFileName = System.getProperty("hippo.config");
        if (hippoConfigFileName == null) {
            logger.info("hippo.config property is not set. Using default values");
            return config;
        }

        try {
            Properties properties = readProperties(hippoConfigFileName);
            setPropertyValues(config, properties);
            return config;
        } catch (FileNotFoundException fnfe) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(hippoConfigFileName + " file is not exists. Please check configuration.");
            }
        } catch (IOException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(e.getMessage());
            }
        }
        return config;
    }

    private static Properties readProperties(String propertyName) throws FileNotFoundException, IOException {
        FileReader reader = null;
        try {
            Properties prop = new Properties();
            reader = new FileReader(propertyName);
            prop.load(reader);
            return prop;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean enableJdbcProfile() {
        return JDBC_PROFILE;
    }

    private static void setPropertyValues(TomcatProfilerConfig config, Properties prop) {
        // TODO : use Properties defaultvalue instead of using temp variable.

        Object temp = null;

        if ((temp = prop.get("SERVER_IP")) != null) {
            config.SERVER_IP = temp.toString();
            if (logger.isLoggable(Level.INFO)) {
                logger.info("SERVER_IP=" + SERVER_IP);
            }
        }
        if ((temp = prop.get("AGENT_TCP_LISTEN_PORT")) != null) {
            config.AGENT_TCP_LISTEN_PORT = Integer.parseInt(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("AGENT_TCP_LISTEN_PORT=" + AGENT_TCP_LISTEN_PORT);
            }
        }
        if ((temp = prop.get("SERVER_TCP_LISTEN_PORT")) != null) {
            config.SERVER_TCP_LISTEN_PORT = Integer.parseInt(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("SERVER_TCP_LISTEN_PORT=" + SERVER_TCP_LISTEN_PORT);
            }
        }
        if ((temp = prop.get("REQUEST_TRANSACTION_DATA_LISTEN_PORT")) != null) {
            config.REQUEST_TRANSACTION_DATA_LISTEN_PORT = Integer.parseInt(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("REQUEST_TRANSACTION_DATA_LISTEN_PORT=" + REQUEST_TRANSACTION_DATA_LISTEN_PORT);
            }
        }
        if ((temp = prop.get("REQUEST_DATA_LISTEN_PORT")) != null) {
            config.REQUEST_DATA_LISTEN_PORT = Integer.parseInt(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("REQUEST_DATA_LISTEN_PORT=" + REQUEST_DATA_LISTEN_PORT);
            }
        }
        if ((temp = prop.get("JVM_DATA_LISTEN_PORT")) != null) {
            config.JVM_DATA_LISTEN_PORT = Integer.parseInt(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("JVM_DATA_LISTEN_PORT=" + JVM_DATA_LISTEN_PORT);
            }
        }
        if ((temp = prop.get("JVM_STAT_GAP")) != null) {
            config.JVM_STAT_GAP = Long.parseLong(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("JVM_STAT_GAP=" + JVM_STAT_GAP);
            }
        }
        if ((temp = prop.get("SERVER_CONNECT_RETRY_GAP")) != null) {
            config.SERVER_CONNECT_RETRY_GAP = Long.parseLong(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("SERVER_CONNECT_RETRY_GAP=" + SERVER_CONNECT_RETRY_GAP);
            }
        }
        if ((temp = prop.get("QUERY_COUNT_OVER_10000")) != null) {
            config.QUERY_COUNT_OVER_10000 = Boolean.parseBoolean(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("QUERY_COUNT_OVER_10000=" + QUERY_COUNT_OVER_10000);
            }
        }
        if ((temp = prop.get("JDBC_PROFILE")) != null) {
            config.JDBC_PROFILE = Boolean.parseBoolean(temp.toString());
            if (logger.isLoggable(Level.INFO)) {
                logger.info("JDBC_PROFILE=" + config.JDBC_PROFILE);
            }
        }

        logger.info("configuration loaded successfully.");
    }
}
