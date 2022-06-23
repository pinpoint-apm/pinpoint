package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.plugin.rabbitmq.util.TestBroker;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;

import java.util.Properties;

public class TestBrokerServer implements SharedTestLifeCycle {
    private TestBroker broker = new TestBroker();
    @Override
    public Properties beforeAll() {
        this.broker = new TestBroker();
        try {
            broker.start();
        } catch (Exception e) {
            throw new RuntimeException("broker start error", e);
        }
        return new Properties();
    }

    @Override
    public void afterAll() {
        if (broker != null) {
            broker.shutdown();
        }
    }
}
