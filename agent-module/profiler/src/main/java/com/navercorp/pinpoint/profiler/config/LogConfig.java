package com.navercorp.pinpoint.profiler.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class LogConfig {

    public static final Path LOGS_DIR = Paths.get("logs");

    private static final String LOG_MAX_BACKUP_SIZE = "profiler.logdir.maxbackupsize";
    private static final int DEFAULT_LOG_MAX_BACKUP_SIZE = 5;

    private final Path logsPath;


    public LogConfig(Path agentLogPath) {

        Objects.requireNonNull(agentLogPath, "agentPath");
        this.logsPath = agentLogPath.resolve(LOGS_DIR);
    }

    public void saveLogFilePath() {
        saveLogFilePath(System.getProperties());
    }

    public void saveLogFilePath(Properties systemProperty) {
        final String path = logsPath.toString();
        // export for log4j2.xml
        systemProperty.setProperty("pinpoint.log", path);
    }

    public void cleanLogDir(Properties properties) {
        String value = properties.getProperty(LOG_MAX_BACKUP_SIZE, String.valueOf(DEFAULT_LOG_MAX_BACKUP_SIZE));
        int logDirMaxBackupSize = Integer.parseInt(value);
        LogDirCleaner logDirCleaner = new LogDirCleaner(logsPath, logDirMaxBackupSize);
        logDirCleaner.clean();
    }
}
