package com.navercorp.pinpoint.test.plugin.api;

import java.util.Properties;

/**
 * @author emeroad
 */
public interface SharedTestLifeCycle {
    Properties beforeAll();
    void afterAll();
}
