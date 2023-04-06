package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class LogLinkView {
    private final boolean logLinkEnable;
    private final String logButtonName;
    private final String disableButtonMessage;
    private final String logLink;

    public LogLinkView(boolean logLinkEnable, String logButtonName, String disableButtonMessage, String logLink) {
        this.logLinkEnable = logLinkEnable;
        this.logButtonName = Objects.requireNonNull(logButtonName, "logButtonName");
        this.disableButtonMessage = Objects.requireNonNull(disableButtonMessage, "disableButtonMessage");
        this.logLink = Objects.requireNonNull(logLink, "logLink");
    }


    @JsonProperty("logLinkEnable")
    public boolean isLogLinkEnable() {
        return this.logLinkEnable;
    }

    @JsonProperty("logButtonName")
    public String getLogButtonName() {
        return this.logButtonName;
    }

    @JsonProperty("logPageUrl")
    public String getLogPageUrl() {
        return this.logLink;
    }

    @JsonProperty("disableButtonMessage")
    public String getDisableButtonMessage() {
        return disableButtonMessage;
    }


}

