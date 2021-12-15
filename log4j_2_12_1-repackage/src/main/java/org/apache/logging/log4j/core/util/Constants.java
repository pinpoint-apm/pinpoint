/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.util;


import org.apache.logging.log4j.util.PropertiesUtil;

/**
 * Log4j Constants.
 */
public final class Constants {

    /**
     * Name of the system property to use to identify the LogEvent factory.
     */
    public static final String LOG4J_LOG_EVENT_FACTORY = "Log4jLogEventFactory";

    /**
     * Name of the system property to use to identify the ContextSelector Class.
     */
    public static final String LOG4J_CONTEXT_SELECTOR = "Log4jContextSelector";

    /**
     * Property name for the default status (internal log4j logging) level to use if not specified in configuration.
     */
    public static final String LOG4J_DEFAULT_STATUS_LEVEL = "Log4jDefaultStatusLevel";

    /**
     * JNDI context name string literal.
     */
    public static final String JNDI_CONTEXT_NAME = "java:comp/env/log4j/context-name";

    /**
     * Number of milliseconds in a second.
     */
    public static final int MILLIS_IN_SECONDS = 1000;

    /**
     * Supports user request LOG4J2-898 to have the option to format a message in the background thread.
     */
    public static final boolean FORMAT_MESSAGES_IN_BACKGROUND = PropertiesUtil.getProperties().getBooleanProperty(
            "log4j.format.msg.async", false);

    /**
     * LOG4J2-2109 if {@code true}, MessagePatternConverter will always operate as though
     * <pre>%m{nolookups}</pre> is configured.
     *
     * @since 2.10
     */
    // ----------------------------------------------------------------------
    // https://github.com/pinpoint-apm/pinpoint/issues/8491
    // Fix CVE-2021-44228 for JDK7
    // ----------------------------------------------------------------------
//    public static final boolean FORMAT_MESSAGES_PATTERN_DISABLE_LOOKUPS = PropertiesUtil.getProperties().getBooleanProperty(
//            "log4j2.formatMsgNoLookups", false);
    public static final boolean FORMAT_MESSAGES_PATTERN_DISABLE_LOOKUPS = true;

    /**
     * {@code true} if we think we are running in a web container, based on the boolean value of system property
     * "log4j2.is.webapp", or (if this system property is not set) whether the  {@code javax.servlet.Servlet} class
     * is present in the classpath.
     */
    public static final boolean IS_WEB_APP = org.apache.logging.log4j.util.Constants.IS_WEB_APP;

    /**
     * Kill switch for object pooling in ThreadLocals that enables much of the LOG4J2-1270 no-GC behaviour.
     * <p>
     * {@code True} for non-{@link #IS_WEB_APP web apps}, disable by setting system property
     * "log4j2.enable.threadlocals" to "false".
     *
     * @since 2.6
     */
    public static final boolean ENABLE_THREADLOCALS = org.apache.logging.log4j.util.Constants.ENABLE_THREADLOCALS;

    /**
     * Kill switch for garbage-free Layout behaviour that encodes LogEvents directly into
     * {@link org.apache.logging.log4j.core.layout.ByteBufferDestination}s without creating intermediate temporary
     * Objects.
     * <p>
     * {@code True} by default iff all loggers are asynchronous because system property
     * {@code Log4jContextSelector} is set to {@code org.apache.logging.log4j.core.async.AsyncLoggerContextSelector}.
     * Disable by setting system property "log4j2.enable.direct.encoders" to "false".
     *
     * @since 2.6
     */
    public static final boolean ENABLE_DIRECT_ENCODERS = PropertiesUtil.getProperties().getBooleanProperty(
            "log4j2.enable.direct.encoders", true); // enable GC-free text encoding by default
    // the alternative is to enable GC-free encoding only by default only when using all-async loggers:
    //AsyncLoggerContextSelector.class.getName().equals(PropertiesUtil.getProperties().getStringProperty(LOG4J_CONTEXT_SELECTOR)));

    /**
     * Initial StringBuilder size used in RingBuffer LogEvents to store the contents of reusable Messages.
     * <p>
     * The default value is {@value}, users can override with system property "log4j.initialReusableMsgSize".
     * </p>
     * @since 2.6
     */
    public static final int INITIAL_REUSABLE_MESSAGE_SIZE = size("log4j.initialReusableMsgSize", 128);

    /**
     * Maximum size of the StringBuilders used in RingBuffer LogEvents to store the contents of reusable Messages.
     * After a large message has been delivered to the appenders, the StringBuilder is trimmed to this size.
     * <p>
     * The default value is {@value}, which allows the StringBuilder to resize three times from its initial size.
     * Users can override with system property "log4j.maxReusableMsgSize".
     * </p>
     * @since 2.6
     */
    public static final int MAX_REUSABLE_MESSAGE_SIZE = size("log4j.maxReusableMsgSize", (128 * 2 + 2) * 2 + 2);

    /**
     * Size of CharBuffers used by text encoders.
     * <p>
     * The default value is {@value}, users can override with system property "log4j.encoder.charBufferSize".
     * </p>
     * @since 2.6
     */
    public static final int ENCODER_CHAR_BUFFER_SIZE = size("log4j.encoder.charBufferSize", 2048);

    /**
     * Default size of ByteBuffers used to encode LogEvents without allocating temporary objects.
     * <p>
     * The default value is {@value}, users can override with system property "log4j.encoder.byteBufferSize".
     * </p>
     * @see org.apache.logging.log4j.core.layout.ByteBufferDestination
     * @since 2.6
     */
    public static final int ENCODER_BYTE_BUFFER_SIZE = size("log4j.encoder.byteBufferSize", 8 * 1024);


    private static int size(final String property, final int defaultValue) {
        return PropertiesUtil.getProperties().getIntegerProperty(property, defaultValue);
    }

    /**
     * Prevent class instantiation.
     */
    private Constants() {
    }
}
