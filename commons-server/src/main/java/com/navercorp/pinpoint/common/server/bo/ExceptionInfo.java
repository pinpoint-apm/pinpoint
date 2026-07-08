package com.navercorp.pinpoint.common.server.bo;

public record ExceptionInfo(int id, String message) {

    /**
     * Placeholder id used for OpenTelemetry-sourced exceptions. Unlike the native agent path,
     * OTel has no {@code StringMetaData} entry backing the exception class name, so the class
     * name cannot be resolved from {@link #id()}. Instead the class name is encoded into
     * {@link #message()} together with the exception message (see {@link #OTEL_MESSAGE_DELIMITER}),
     * and this id is left as a fixed placeholder. The web side distinguishes the OTel encoding
     * via the span's {@code TraceSourceType}, not via this id.
     */
    public static final int OTEL_EXCEPTION_ID = 0;

    /**
     * Separates the exception class name from the exception message inside {@link #message()}
     * for OpenTelemetry-sourced exceptions, encoded as {@code "<className><delimiter><message>"}.
     * <p>
     * The class name is always written as the prefix (an empty string when unknown) and never
     * contains this character (Java fully-qualified class names have no {@code ':'}), so a reader
     * splits on the FIRST occurrence: the prefix is the class name (empty ⇒ unknown) and the
     * remainder — which may itself contain {@code ':'} — is the message.
     */
    public static final char OTEL_MESSAGE_DELIMITER = ':';

    /**
     * Extracts the exception class name from an OTel-encoded {@link #message()} — the prefix
     * before the first {@link #OTEL_MESSAGE_DELIMITER}. Returns {@code null} when the class name
     * is unknown (empty prefix / no delimiter), so the caller renders no exception type.
     */
    public static String otelClassName(String message) {
        if (message == null) {
            return null;
        }
        final int idx = message.indexOf(OTEL_MESSAGE_DELIMITER);
        if (idx <= 0) {
            return null;
        }
        return message.substring(0, idx);
    }

    /**
     * Extracts the exception message from an OTel-encoded {@link #message()} — everything after
     * the first {@link #OTEL_MESSAGE_DELIMITER} (which may itself contain the delimiter). Falls
     * back to the whole string when no delimiter is present.
     */
    public static String otelMessageBody(String message) {
        if (message == null) {
            return null;
        }
        final int idx = message.indexOf(OTEL_MESSAGE_DELIMITER);
        if (idx < 0) {
            return message;
        }
        return message.substring(idx + 1);
    }
}
