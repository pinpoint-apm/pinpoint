package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.config.LogProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LogLinkBuilder {
    private final LogProperties logProperties;

    public LogLinkBuilder(LogProperties logProperties) {
        this.logProperties = Objects.requireNonNull(logProperties, "logProperties");
    }

    public LogLinkView build(TransactionId transactionId, long spanId, String applicationName, long startTime) {
        String logLinkUrl = buildLogLinkUrl(logProperties.getLogPageUrl(), transactionId, spanId, applicationName, startTime);

        return new LogLinkView(logProperties.isLogLinkEnable(),
                logProperties.getLogButtonName(),
                logProperties.getDisableButtonMessage(),
                logLinkUrl);
    }

    String buildLogLinkUrl(String logPageUrl, TransactionId txId, long spanId, String applicationName, long startTime) {
        if (StringUtils.isNotEmpty(logPageUrl)) {
            final String parameter = "transactionId=" + txId +
                    "&spanId=" + spanId +
                    "&applicationName=" + applicationName +
                    "&time=" + startTime;

            return logPageUrl + "?" + parameter;
        }

        return "";
    }
}
