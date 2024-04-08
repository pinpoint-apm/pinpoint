package com.navercorp.pinpoint.profiler.logging;

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.logging.PluginLoggerBinder;
import com.navercorp.pinpoint.profiler.logging.jul.JulAdaptorHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Log4j2LoggingSystem implements LoggingSystem {
    public static final String CONTEXT_NAME = "pinpoint-agent-logging-context";

    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";
    public static final String NOLOOKUPS = "log4j2.formatMsgNoLookups";

    private static final String[] LOOKUP = {
            "log4j2-test.properties", "log4j2-test.xml",
            "log4j2-agent.properties", "log4j2-agent.xml",
    };

    private LoggerContext loggerContext;
    private final Path profilePath;

    private PluginLoggerBinder binder;


    public Log4j2LoggingSystem(Path profilePath) {
        this.profilePath = Objects.requireNonNull(profilePath, "profilePath");
    }

    public void start() {
        // log4j init
        Path configLocation = getConfigPath(profilePath);
        URI uri = configLocation.toUri();

        BootLogger bootLogger = BootLogger.getLogger(this.getClass());
        bootLogger.info("logPath:" + uri);

        this.loggerContext = getLoggerContext(uri);
//        this.loggerContext = getLoggerContext2(uri);

        Logger logger = getLoggerContextLogger();
        logger.info("{} start", this.getClass().getSimpleName());

        logger.info("LoggerContextFactory:{} LoggerContext:{}", LogManager.getFactory().getClass().getName(), loggerContext.getClass().getName());

        this.binder = new Log4j2Binder(loggerContext);
        bindPLoggerFactory(this.binder);

        this.setupGrpcLogger(loggerContext);
    }


    private void setupGrpcLogger(LoggerContext loggerContext) {
        final Logger logger = loggerContext.getLogger(this.getClass().getName());

        String key = "pinpoint.profiler.grpc.log.enable";
        final boolean enableGrpcLog = Boolean.parseBoolean(System.getProperty(key));
        logger.info("{}:{}", key, enableGrpcLog);
        if (!enableGrpcLog) {
            return;
        }

        final Handler handler = new JulAdaptorHandler(loggerContext);
        logger.info("java.util.logging.LogManager={}", java.util.logging.LogManager.getLogManager().getClass().getName());
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("io.grpc");

        Level level = julLogger.getLevel();
        handler.setLevel(Level.FINE);
        logger.info("io.grpc log level:{}", level);
        julLogger.setLevel(Level.FINE);
        julLogger.addHandler(handler);
        julLogger.info("enable grpc log");
    }


    private Logger getLoggerContextLogger() {
        Logger logger = loggerContext.getLogger(getClass().getName());
        return logger;
    }


    private Path getConfigPath(Path profilePath) {
        for (String configFile : LOOKUP) {
            Path configLocation = profilePath.resolve(configFile);
            if (configLocation.toFile().exists()) {
                return configLocation;
            }
        }
        throw new IllegalStateException("log4j2.xml not found");
    }

    private LoggerContext getLoggerContext(URI uri) {
        // Prepare SystemProperties
        final String factory = prepare(FACTORY_PROPERTY_NAME, Log4j2ContextFactory.class.getName());
        // Log4j2 RCE CVE-2021-44228
        // https://github.com/pinpoint-apm/pinpoint/issues/8489
        final String nolookup = prepare(NOLOOKUPS, Boolean.TRUE.toString());
        try {
            return LogManager.getContext(this.getClass().getClassLoader(), false, null, uri, CONTEXT_NAME);
        } finally {
            rollback(NOLOOKUPS, nolookup);
            rollback(FACTORY_PROPERTY_NAME, factory);
        }
    }

//    private LoggerContext getLoggerContext2(URI uri) {
//        ContextSelector selector = new ClassLoaderContextSelector();
//        ShutdownCallbackRegistry shutdownCallbackRegistry = new DefaultShutdownCallbackRegistry();
//        Log4jContextFactory factory = new Log4jContextFactory(selector, shutdownCallbackRegistry);
//
//        LoggerContext old = (LoggerContext)LogManager.getContext();
//        old.stop();
//
//        LogManager.setFactory(factory);
//
//        return (LoggerContext) LogManager.getContext(this.getClass().getClassLoader(), false, null, uri, CONTEXT_NAME);
//    }


    public void stop() {
        if (loggerContext != null) {
            Logger logger = getLoggerContextLogger();
            logger.info("{} stop", this.getClass().getSimpleName());
            this.binder.shutdown();
            if (loggerContext instanceof LifeCycle) {
                logger.info("{} loggerContext stop", this.getClass().getSimpleName());
                ((LifeCycle) loggerContext).stop();
            }
        }

    }

    private String prepare(String key, String value) {
        final String backup = System.getProperty(key);
        System.setProperty(key, value);
        return backup;
    }

    private void rollback(String key, String backup) {
        if (backup != null) {
            System.setProperty(key, backup);
        } else {
            System.clearProperty(key);
        }
    }


    private void bindPLoggerFactory(PluginLoggerBinder binder) {
        final String binderClassName = binder.getClass().getName();
        PluginLogger pluginLogger = binder.getLogger(binder.getClass().getName());
        pluginLogger.info("PLoggerFactory.initialize() bind:{} cl:{}", binderClassName, binder.getClass().getClassLoader());
        // Set binder to static LoggerFactory
        // Should we unset binder at shutdown hook or stop()?
        PluginLogManager.initialize(binder);
    }
}