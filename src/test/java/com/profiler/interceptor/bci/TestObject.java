package com.profiler.interceptor.bci;

import java.util.logging.Logger;

public class TestObject {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private int callA;

    public int callA(){
        logger.info("callA");
        int i = callA++;
        return i;
    }

    public String  hello(String a) {
        System.out.println("a:" + a);
        System.out.println("test");
//        throw new RuntimeException("test");
        return "a";
    }

}
