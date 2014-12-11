package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung
 */
public class AlarmMailTemplate {

    private static final String LINE_FEED = "<br>";
    private static final String LINK_FORMAT = "<a href=\"%s\" >%s</a>";

    private final String pinpointUrl;
    private final AlarmChecker checker;
    
    public AlarmMailTemplate(AlarmChecker checker, String pinpointUrl) {
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
        body.append(checker.getEmailMessage());
        body.append(String.format(LINK_FORMAT, pinpointUrl, pinpointUrl));
        
        return body.toString();
    }
}
