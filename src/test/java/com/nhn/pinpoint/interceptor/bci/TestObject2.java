package com.nhn.pinpoint.interceptor.bci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestObject2 {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public int callA;
    public int callB;

    public int callA(){
        logger.info("callA");
        int i = callA++;
        return i;
    }

    public void callB() {
        callB++;
    }


}
