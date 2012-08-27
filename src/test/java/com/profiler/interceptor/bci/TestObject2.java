package com.profiler.interceptor.bci;

import java.util.logging.Logger;

public class TestObject2 {
    private Logger logger = Logger.getLogger(this.getClass().getName());

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
