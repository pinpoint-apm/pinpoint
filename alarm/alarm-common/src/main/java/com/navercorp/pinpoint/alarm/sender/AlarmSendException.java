/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.sender;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class AlarmSendException extends RuntimeException {

    /** Enough of a rejection to read the reason, such as Slack's {@code invalid_payload}. */
    private static final int MAX_RESPONSE_BODY_CHARS = 200;

    public AlarmSendException(String message) {
        super(message);
    }

    public AlarmSendException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * A failure and its causes, one per link, for a log line. Spring puts the request url in the
     * message of a client exception, and a provider such as Slack puts the credential in the url,
     * so those show the class, the status and the start of the response body. Each link stays in
     * the cause chain itself, where the retry decision reads it.
     */
    public static String describe(Throwable failure) {
        StringBuilder sb = new StringBuilder();
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (!sb.isEmpty()) {
                sb.append(" <- ");
            }
            sb.append(current.getClass().getSimpleName());
            if (current instanceof RestClientResponseException response) {
                sb.append(" status=").append(response.getStatusCode().value());
                String body = response.getResponseBodyAsString();
                if (StringUtils.isNotBlank(body)) {
                    // the receiver wrote it, so it must not start a log line of its own
                    String oneLine = body.replaceAll("\\p{Cntrl}", " ").strip();
                    sb.append(" body=").append(StringUtils.abbreviate(oneLine, MAX_RESPONSE_BODY_CHARS));
                }
            } else if (!(current instanceof RestClientException) && current.getMessage() != null) {
                sb.append(": ").append(current.getMessage());
            }
        }
        return sb.toString();
    }

    /**
     * The failure, for a log call to print its stack trace, unless a client exception in its chain
     * would print the url with it; then null, and {@link #describe} is all the log gets.
     */
    public static Throwable stackTraceOf(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof RestClientException) {
                return null;
            }
        }
        return failure;
    }
}
