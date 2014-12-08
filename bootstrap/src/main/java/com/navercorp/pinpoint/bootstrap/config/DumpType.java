package com.nhn.pinpoint.bootstrap.config;

/**
 * @author emeroad
 */
public enum DumpType {
//  NONE(-1),  comment out becasue of duplicated configuration. 
    ALWAYS(0), EXCEPTION(1);

    private int code;
    private DumpType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


}
