package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PinotAlarmMailTemplate {

    private static final String LINE_FEED = "<br>";
    private static final String LINK_FORMAT = "<a href=\"%s\" >pinpoint site</a>";
    private static final String STAT_LINK_FORMAT = "<a href=\"%s/%s/%s/5m/%s\" > statistics page of %s</a>";
    private static final String LOGO_FORMAT = "<img src=\"data:image/png;base64,%s\" alt=\"pinpoint\" />";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final String pinpointUrl;
    private final PinotAlarmCheckerInterface checker;
    private final int index;
    private final String batchEnv;
    private final String logoBase64;

    public PinotAlarmMailTemplate(String pinpointUrl, String batchEnv, PinotAlarmCheckerInterface checker, int index, String logoBase64) {
        this.pinpointUrl = pinpointUrl;
        this.batchEnv = batchEnv;
        this.checker = checker;
        this.index  = index;
        this.logoBase64 = logoBase64;
    }
    public String createSubject() {
        return String.format("[PINPOINT-%s] %s Alarm for %s %s.", batchEnv, checker.getCheckerName(index), checker.getApplicationName(), checker.getTarget());
    }

    public String createBody() {
        return newBody(createSubject(), checker.getRule(index), checker.getMenuUrl(), checker.getApplicationName(), getCurrentTime());
    }

    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return FORMATTER.format(now);
    }

    private String newBody(String subject, String rule, String menuUrl, String applicationName, String currentTime) {
        StringBuilder body = new StringBuilder();
        if (StringUtils.hasLength(logoBase64)) {
            body.append(String.format(LOGO_FORMAT, logoBase64));
            body.append(LINE_FEED);
        }
        body.append("<strong>").append(subject).append("</strong>");
        body.append(LINE_FEED);
        body.append(LINE_FEED);
        body.append(String.format("Rule : %s", rule));
        body.append(LINE_FEED);
        body.append(checker.getEmailMessage(index));
        body.append(String.format(LINK_FORMAT, pinpointUrl));
        body.append(LINE_FEED);
        body.append(String.format(STAT_LINK_FORMAT, pinpointUrl, menuUrl, applicationName, currentTime, applicationName));

        return body.toString();
    }
}
