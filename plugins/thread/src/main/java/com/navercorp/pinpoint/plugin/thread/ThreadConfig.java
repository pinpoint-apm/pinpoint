package com.navercorp.pinpoint.plugin.thread;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author echo
 */
public class ThreadConfig {
    public static final String RUNNABLE = "java.lang.Runnable";
    public static final String CALLABLE = "java.util.concurrent.Callable";
    public static final String SUPPLIER = "java.util.function.Supplier";

    private final String threadMatchPackage;
    private final boolean runnable;
    private final boolean callable;
    private final boolean supplier;

    public ThreadConfig(ProfilerConfig config) {
        this.threadMatchPackage = config.readString("profiler.thread.match.package", null);

        List<String> types = config.readList("profiler.thread.support-class");
        this.runnable = supportType(types, "Runnable");
        this.callable = supportType(types, "Callable");
        this.supplier = supportType(types, "Supplier");
    }

    private boolean supportType(List<String> types, String supportType) {
        for (String type : types) {
            if (type.equalsIgnoreCase(supportType)) {
                return true;
            }
        }
        return false;
    }

    public String getThreadMatchPackage() {
        return threadMatchPackage;
    }

    public boolean isRunnableSupport() {
        return runnable;
    }

    public boolean isCallableSupport() {
        return callable;
    }

    public boolean isSupplierSupport() {
        return supplier;
    }

    @Override
    public String toString() {
        return "ThreadConfig{" +
                "threadMatchPackage='" + threadMatchPackage + '\'' +
                ", supplier=" + supplier +
                ", runnable=" + runnable +
                ", callable=" + callable +
                '}';
    }
}
