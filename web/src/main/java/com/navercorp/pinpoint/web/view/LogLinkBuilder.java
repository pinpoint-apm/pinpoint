package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.web.config.LogConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class LogLinkBuilder {
    private final LogConfiguration logConfiguration;

    public LogLinkBuilder(LogConfiguration logConfiguration) {
        this.logConfiguration = Objects.requireNonNull(logConfiguration, "logConfiguration");
    }

    public LogLinkView build(TransactionId transactionId, long spanId, String applicationId, long startTime) {
        String txId = TransactionIdUtils.formatString(transactionId);
        String logLinkUrl = buildLogLinkUrl(logConfiguration.getLogPageUrl(), txId, spanId, applicationId, startTime);

        return new LogLinkView(logConfiguration.isLogLinkEnable(),
                logConfiguration.getLogButtonName(),
                logConfiguration.getDisableButtonMessage(),
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
