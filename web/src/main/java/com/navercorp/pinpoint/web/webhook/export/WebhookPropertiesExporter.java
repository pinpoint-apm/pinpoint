package com.navercorp.pinpoint.web.webhook.export;

import com.navercorp.pinpoint.common.server.frontend.export.FrontendConfigExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebhookPropertiesExporter implements FrontendConfigExporter {

    @Value("${pinpoint.modules.web.webhook:false}")
    private boolean webhookEnable;
    public WebhookPropertiesExporter() {
    }

    @Override
    public void export(Map<String, Object> export) {
        export.put("webhookEnable", webhookEnable);
    }
}
