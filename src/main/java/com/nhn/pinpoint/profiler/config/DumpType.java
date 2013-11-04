package com.nhn.pinpoint.profiler.config;

/**
 * @author emeroad
 */
public enum DumpType {
//    NONE(-1), 의 경우 이미 더 앞단에서 할지 말지를 결정하므로 중복설정이라 주석처리함.
    ALWAYS(0), EXCEPTION(1);

    private int code;
    private DumpType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


}
