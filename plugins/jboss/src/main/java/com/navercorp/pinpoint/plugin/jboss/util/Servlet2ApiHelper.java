package com.navercorp.pinpoint.plugin.jboss.util;

import org.apache.catalina.connector.Response;

import javax.servlet.DispatcherType;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlet2ApiHelper implements ServletApiHelper {
    public Servlet2ApiHelper() {
    }

    @Override
    public boolean isAsyncDispatcherBefore(HttpServletRequest request) {
        return false;
    }

    @Override
    public boolean isAsyncDispatcherAfter(HttpServletRequest request) {
        return false;
    }

    @Override
    public int getStatus(HttpServletResponse response) {
        if (response instanceof Response) {
            // JBoss 4 (Spring 2.X)
            final Response r = (Response) response;
            return r.getStatus();
        }
        return 0;
    }
}
