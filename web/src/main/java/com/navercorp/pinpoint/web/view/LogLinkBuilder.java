package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
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

    public LogLinkView build(TransactionId transactionId, long spanId, String applicationId, long startTime) {
        String txId = TransactionIdUtils.formatString(transactionId);
        String logLinkUrl = buildLogLinkUrl(logProperties.getLogPageUrl(), txId, spanId, applicationId, startTime);

        return new LogLinkView(logProperties.isLogLinkEnable(),
                logProperties.getLogButtonName(),
                logProperties.getDisableButtonMessage(),
                logLinkUrl);
    }

    String buildLogLinkUrl(String logPageUrl, String txId, long spanId, String applicationId, long startTime) {
        if (StringUtils.isNotEmpty(logPageUrl)) {
            final String parameter = "transactionId=" + txId +
                    "&spanId=" + spanId +
                    "&applicationName=" + applicationId +
                    "&time=" + startTime;

            return logPageUrl + "?" + parameter;
        }

        return "";
    }
}
