package com.navercorp.pinpoint.profiler.logging;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.logging.PluginLoggerBinder;
import com.navercorp.pinpoint.profiler.logging.jul.JulAdaptorHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Log4j2LoggingSystem implements LoggingSystem {
    public static final String CONTEXT_NAME = "pinpoint-agent-logging-context";

    private static final String[] LOOKUP = {
            "log4j2-test.properties", "log4j2-test.xml",
            "log4j2-agent.properties", "log4j2-agent.xml",
    };

    private LoggerContext loggerContext;
    private final Path configLocation;

    private PluginLoggerBinder binder;


    public Log4j2LoggingSystem(Path agentPath) {
        Objects.requireNonNull(agentPath, "agentPath");
        this.configLocation = getConfigPath(agentPath);
    }

    @Override
    public String getConfigLocation() {
        return configLocation.toString();
    }

    @Override
    public void start() {
        // log4j init

        this.loggerContext = getLoggerContext();

        Logger logger = getLoggerContextLogger();
        logger.info("{} start logPath:{}", this.getClass().getSimpleName(), configLocation);

        logger.info("LoggerContextFactory:{}", LogManager.getFactory().getClass().getName());
        logger.info("LoggerContext:{}", loggerContext.getClass().getName());

        this.binder = new Log4j2Binder(loggerContext);
        bindPluginLogFactory(this.binder);

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
        return loggerContext.getLogger(getClass().getName());
    }


    private Path getConfigPath(Path profilePath) {
        for (String configFile : LOOKUP) {
            Path configLocation = profilePath.resolve(configFile);
            if (Files.exists(configLocation)) {
                return configLocation;
            }
        }
        throw new IllegalStateException("'log4j2-agent.xml' not found. agentPath:" + profilePath);
    }

    private LoggerContext getLoggerContext() {
        Log4jEnvExecutor executor = new Log4jEnvExecutor();
        return executor.call(this::newLoggerContext);
    }

    public LoggerContext newLoggerContext() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URI uri = configLocation.toUri();
        return LogManager.getContext(classLoader, false, null, uri, CONTEXT_NAME);
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


    @Override
    public void close() {
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


    private void bindPluginLogFactory(PluginLoggerBinder binder) {
        final String binderClassName = binder.getClass().getName();
        PluginLogger pluginLogger = binder.getLogger(binder.getClass().getName());
        pluginLogger.info("PluginLogManager.initialize() bind:{} cl:{}", binderClassName, binder.getClass().getClassLoader());
        // Set binder to static PluginLogManager
        // Should we unset binder at shutdown hook or stop()?
        if (!PluginLogManager.initialize(binder)) {
            pluginLogger.warn("LoggerBinder is already initialized");
        }
    }
}
