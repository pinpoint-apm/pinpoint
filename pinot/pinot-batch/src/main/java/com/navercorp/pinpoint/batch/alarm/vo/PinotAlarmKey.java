package com.navercorp.pinpoint.batch.alarm.vo;

import java.util.Objects;

public class PinotAlarmKey {
    private final String serviceName;
    private final String applicationName;
    private final String target;
    private final String categoryName;

    public PinotAlarmKey(String serviceName, String applicationName, String target, String categoryName) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationName = Objects.requireNonNull(applicationName, "name");
        this.target = Objects.requireNonNull(target, "target");
        this.categoryName = Objects.requireNonNull(categoryName, "alarmCategory");;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getTarget() {
        return target;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PinotAlarmKey that = (PinotAlarmKey) o;

        if (!serviceName.equals(that.serviceName)) return false;
        if (!applicationName.equals(that.applicationName)) return false;
        if (!target.equals(that.target)) return false;

        return categoryName.equals(that.categoryName);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + categoryName.hashCode();
        return result;
    }
}
