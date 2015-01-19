package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public interface ScopeDefinition {
    enum Type {
        SIMPLE, ATTACHMENT
    }

    String getName();

    Type getType();
}
