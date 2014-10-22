package com.nhn.pinpoint.profiler.context;

import java.util.List;

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
