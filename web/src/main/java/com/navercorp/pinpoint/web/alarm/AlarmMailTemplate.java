package com.nhn.pinpoint.web.alarm;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class AlarmMailTemplate {

    private static final String LINE_FEED = "<br>";
    private static final String LINK_FORMAT = "<a href=\"%s\" >%s</a>";

    private final String pinpointUrl;
    private final AlarmCheckFilter checker;
    
    public AlarmMailTemplate(AlarmCheckFilter checker, String pinpointUrl) {
        this.checker = checker;
        this.pinpointUrl =pinpointUrl;
    }
    
    public String createSubject() {
        Rule rule = checker.getRule();
        return String.format("[PINPOINT-DEV] %s Alarm for %s Service.", rule.getCheckerName(), rule.getApplicationId());
    }

    public String createBody() {
        Rule rule = checker.getRule();

        StringBuilder body = new StringBuilder();
        body.append("<strong>" + createSubject() + "</strong>");
        body.append(LINE_FEED);
        body.append(LINE_FEED);
        body.append(String.format("Rule : %s", rule.getCheckerName()));
        body.append(LINE_FEED);
        body.append(String.format("%s value is %s during the past 5 mins.(Threshold : %s%s)", rule.getCheckerName(), checker.getDetectedValue(), rule.getThreshold(), checker.getUnit()));
        body.append(LINE_FEED);
        body.append(String.format(LINK_FORMAT, pinpointUrl, pinpointUrl));
        
        return body.toString();
    }
}
