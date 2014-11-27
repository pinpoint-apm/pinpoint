package com.nhn.pinpoint.test;

import java.util.List;

import com.nhn.pinpoint.profiler.context.DefaultServerMetaDataHolder;

/**
 * @author hyungil.jeong
 */
public class ResettableServerMetaDataHolder extends DefaultServerMetaDataHolder {

    public ResettableServerMetaDataHolder(List<String> vmArgs) {
        super(vmArgs);
    }

    public void reset() {
        this.serverName = null;
        this.serviceInfos.clear();
    }

}
