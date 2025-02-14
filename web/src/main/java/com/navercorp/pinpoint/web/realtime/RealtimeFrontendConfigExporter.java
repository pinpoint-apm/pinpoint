package com.navercorp.pinpoint.web.realtime;

import com.navercorp.pinpoint.common.server.frontend.export.FrontendConfigExporter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RealtimeFrontendConfigExporter implements FrontendConfigExporter {

    private final boolean showActiveThread;
    private final boolean showActiveThreadDump;

    public RealtimeFrontendConfigExporter(boolean showActiveThread, boolean showActiveThreadDump) {
        this.showActiveThread = showActiveThread;
        this.showActiveThreadDump = showActiveThreadDump;
    }

    @Override
    public void export(Map<String, Object> export) {
        export.put("showActiveThread", this.showActiveThread);
        export.put("showActiveThreadDump", this.showActiveThreadDump);
    }
}
