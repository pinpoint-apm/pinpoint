package com.navercorp.pinpoint.profiler.cache;

import org.junit.jupiter.api.Test;

class CaffeineBuilderTest {


    @Test
    void executor_manager() {
        CaffeineBuilder.newBuilder();
        CaffeineBuilder.MANAGER.shutdown();
    }

    @Test
    void executor_manager_null_check() {
        CaffeineBuilder.newBuilder();
        CaffeineBuilder.newBuilder();
        CaffeineBuilder.MANAGER.shutdown();

        CaffeineBuilder.newBuilder();
        CaffeineBuilder.MANAGER.shutdown();
        CaffeineBuilder.MANAGER.shutdown();
    }
}