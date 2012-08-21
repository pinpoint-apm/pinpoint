package com.profiler.interceptor;

public class TestObject{

    public void hello() {
        System.out.println("hello");
        throw new RuntimeException();
    }
}
