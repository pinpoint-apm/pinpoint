package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.bootstrap.agentdir.LogDirCleaner;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class LogConfig {
    private final BootLogger logger = BootLogger.getLogger(getClass());

    private final Properties systemProperty;

    public LogConfig() {
        this(System.getProperties());
    }

    public LogConfig(Properties properties) {
        this.systemProperty = Objects.requireNonNull(properties, "properties");
    }

    public void saveLogFilePath(Path agentLogFilePath) {
        final String path = agentLogFilePath.toString();
        logger.info(String.format("logPath:%s", path));
        systemProperty.setProperty(ProductInfo.NAME + ".log", path);
    }

    public void cleanLogDir(Path agentLogFilePath, final int logDirMaxBackupSize) {
        logger.info("Log directory maxbackupsize=" + logDirMaxBackupSize);
        LogDirCleaner logDirCleaner = new LogDirCleaner(agentLogFilePath, logDirMaxBackupSize);
        logDirCleaner.clean();
    }
}
