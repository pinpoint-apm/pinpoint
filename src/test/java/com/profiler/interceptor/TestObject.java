package com.profiler.interceptor;

public class TestObject{

    public String  hello(String a) {
        System.out.println("a:" + a);
        System.out.println("test");
//        throw new RuntimeException("test");
        return "a";
    }

}
