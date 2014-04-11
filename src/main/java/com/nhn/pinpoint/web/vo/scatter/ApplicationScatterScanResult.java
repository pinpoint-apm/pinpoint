package com.nhn.pinpoint.web.vo.scatter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhn.pinpoint.web.view.ApplicationScatterScanResultSerializer;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.scatter.ScatterScanResult;

/**
 * @author emeroad
 */
@JsonSerialize(using = ApplicationScatterScanResultSerializer.class)
public class ApplicationScatterScanResult {

    private final Application application;
    private final ScatterScanResult scatterScanResult;

    public ApplicationScatterScanResult(Application application, ScatterScanResult scatterScanResult) {
        this.application = application;
        this.scatterScanResult = scatterScanResult;
    }

    public Application getApplication() {
        return application;
    }

    public ScatterScanResult getScatterScanResult() {
        return scatterScanResult;
    }
}
