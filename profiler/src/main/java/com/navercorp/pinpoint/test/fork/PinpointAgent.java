package com.nhn.pinpoint.test.fork;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nhn.pinpoint.common.Version;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PinpointAgent {
    String value() default "target/pinpoint-agent";
    String version() default Version.VERSION; 
}
