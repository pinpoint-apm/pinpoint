package com.nhn.pinpoint.profiler.modifier.spring.beans;

public class Outer {
    private Inner inner;
    
    public void setInner(Inner inner) {
        this.inner = inner;
    }
    
    public Inner getInner() {
        return inner;
    }

    public void doSomething() {
        
    }
}
