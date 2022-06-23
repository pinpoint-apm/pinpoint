package com.navercorp.pinpoint.test.plugin.shared;

import java.util.Properties;

/**
 * @author emeroad
 */
public interface SharedTestLifeCycle {
    Properties beforeAll();
    void afterAll();
}
