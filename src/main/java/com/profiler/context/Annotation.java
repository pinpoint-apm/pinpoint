package com.profiler.context;

public enum Annotation {
    @Deprecated
    ClientSend("CS"),
    @Deprecated
    ClientRecv("CR"),
    @Deprecated
    ServerSend("SS"),
    @Deprecated
    ServerRecv("SR");

    private String code;

    Annotation(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
