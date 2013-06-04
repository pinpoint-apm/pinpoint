package com.nhn.pinpoint.profiler.interceptor.bci;

public enum Type {
	around(0), before(1), after(2), auto(3);
    private int a;
    private Type(int a) {
        this.a = a;
    }
}
