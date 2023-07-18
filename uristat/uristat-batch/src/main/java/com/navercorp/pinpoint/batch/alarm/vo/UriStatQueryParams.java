package com.navercorp.pinpoint.batch.alarm.vo;

import com.navercorp.pinpoint.common.server.util.time.Range;

public class UriStatQueryParams {
    final private String tenantId;
    final private String serviceName;
    final private String applicationName;
    final private String targetUri;
    final private Range range;

    public UriStatQueryParams(String tenantId, String serviceName, String applicationName, String targetUri, Range range) {
        this.tenantId = tenantId;
        this.serviceName = serviceName;
        this.applicationName = applicationName;
        this.targetUri = targetUri;
        this.range = range;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public Range getRange() {
        return range;
    }
}
