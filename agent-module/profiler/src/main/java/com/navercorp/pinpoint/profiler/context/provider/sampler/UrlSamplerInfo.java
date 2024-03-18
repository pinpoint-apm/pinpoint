package com.navercorp.pinpoint.profiler.context.provider.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;

public class UrlSamplerInfo {
    private String urlPath;
    private Sampler sampler;
    private int samplingNewThroughput;
    private int samplingContinueThroughput;

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public void setSampler(Sampler sampler) {
        this.sampler = sampler;
    }

    public int getSamplingNewThroughput() {
        return samplingNewThroughput;
    }

    public void setSamplingNewThroughput(int samplingNewThroughput) {
        this.samplingNewThroughput = samplingNewThroughput;
    }

    public int getSamplingContinueThroughput() {
        return samplingContinueThroughput;
    }

    public void setSamplingContinueThroughput(int samplingContinueThroughput) {
        this.samplingContinueThroughput = samplingContinueThroughput;
    }

    public boolean isValid() {
        if (this.urlPath == null) {
            return false;
        }

        if (this.sampler == null) {
            return false;
        }

        return true;
    }
}
