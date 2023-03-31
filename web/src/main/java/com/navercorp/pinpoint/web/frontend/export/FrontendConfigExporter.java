package com.navercorp.pinpoint.web.frontend.export;

import java.util.Map;

@FunctionalInterface
public interface FrontendConfigExporter {
    void export(Map<String, Object> export);
}
