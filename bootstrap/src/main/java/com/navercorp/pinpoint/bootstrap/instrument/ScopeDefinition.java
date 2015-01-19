package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public interface ScopeDefinition {
    enum ScopeType {
        SIMPLE, ATTACHMENT
    }

    String getName();

    ScopeType getType();
}
