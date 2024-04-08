package com.navercorp.pinpoint.bootstrap.logging;

/**
 * @deprecated Since 3.0.0 Use {@link com.navercorp.pinpoint.bootstrap.logging.PluginLogManager} instead.
 */
@Deprecated
public class PLoggerFactory {

    public static void initialize(PluginLoggerBinder loggerBinder) {
        PluginLogManager.initialize(loggerBinder);
    }

    public static void unregister(PluginLoggerBinder loggerBinder) {
        PluginLogManager.unregister(loggerBinder);
    }

    public static PLogger getLogger(String name) {
        return (PLogger) PluginLogManager.getLogger(name);

    }

    public static PLogger getLogger(Class<?> clazz) {
        return (PLogger) PluginLogManager.getLogger(clazz);
    }
}
