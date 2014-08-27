package com.nhn.pinpoint.web.service.map;

import com.nhn.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public class AcceptApplication {
    private final String host;
    private final Application application;

    public AcceptApplication(String host, String applicationName, short serviceCode) {
        this(host, new Application(applicationName, serviceCode));
    }

    public AcceptApplication(String host, Application application) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        this.application = application;
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcceptApplication that = (AcceptApplication) o;

        if (!application.equals(that.application)) return false;
        if (!host.equals(that.host)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + application.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AcceptApplication{");
        sb.append("host='").append(host).append('\'');
        sb.append(", application=").append(application);
        sb.append('}');
        return sb.toString();
    }
}
