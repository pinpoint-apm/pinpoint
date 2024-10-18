package com.navercorp.pinpoint.bootstrap.plugin.request;

public interface ApplicationInfoSender<REQ> {

    void sendCallerApplicationName(REQ request);

}
