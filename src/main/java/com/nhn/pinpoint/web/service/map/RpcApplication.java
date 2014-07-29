package com.nhn.pinpoint.web.service.map;

import com.nhn.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public class RpcApplication {
    private final String host;
    private final Application application;

    public RpcApplication(String rpcUrl, Application sourceApplication) {
        if (rpcUrl == null) {
            throw new NullPointerException("rpcUrl must not be null");
        }
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }

        this.host = rpcUrl;
        this.application = sourceApplication;
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

        RpcApplication that = (RpcApplication) o;

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
}
