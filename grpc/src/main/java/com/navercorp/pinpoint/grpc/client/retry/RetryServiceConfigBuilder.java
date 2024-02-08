/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.client.retry;

import io.grpc.Status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RetryServiceConfigBuilder implements ServiceConfigBuilder {

    public static final double DEFAULT_MAX_ATTEMPTS = 3.0;
    public static final long DEFAULT_INITIAL_BACKOFF_MILLIS = 1000L;
    public static final long DEFAULT_MAX_BACKOFF_MILLIS = 4000L;
    public static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
    public static final List<String> DEFAULT_RETRYABLE_STATUS_CODES = Collections.singletonList(Status.Code.UNAVAILABLE.name());

    private Double maxAttempts = DEFAULT_MAX_ATTEMPTS; //Required. Must be two or greater
    private String initialBackoff = millisToString(DEFAULT_INITIAL_BACKOFF_MILLIS); //Required. Long decimal with "s" appended
    private String maxBackoff = millisToString(DEFAULT_MAX_BACKOFF_MILLIS); //Required. Long decimal with "s" appended
    private Double backoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER; //Required. Must be greater than zero.
    private List<String> retryableStatusCodes; //Required and must be non-empty (eg. [14], ["UNAVAILABLE"] or ["unavailable"])

    @Override
    public Map<String, ?> buildMetadataConfig() {
        Map<String, Object> methodConfig = new LinkedHashMap<>();
        addMetadataService(methodConfig);
        addRetryPolicy(methodConfig);
        return Collections.singletonMap("methodConfig", Collections.singletonList(methodConfig));
    }

    private void addMetadataService(Map<String, Object> methodConfig) {
        Map<String, Object> service = Collections.singletonMap("service", METADATA_SERVICE);
        methodConfig.put("name", Collections.singletonList(service));
    }

    private void addRetryPolicy(Map<String, Object> methodConfig) {
        Map<String, Object> retryPolicy = new LinkedHashMap<>();
        retryPolicy.put("maxAttempts", maxAttempts);
        retryPolicy.put("initialBackoff", initialBackoff);
        retryPolicy.put("maxBackoff", maxBackoff);
        retryPolicy.put("backoffMultiplier", backoffMultiplier);
        if (retryableStatusCodes == null || retryableStatusCodes.isEmpty()) {
            retryableStatusCodes = DEFAULT_RETRYABLE_STATUS_CODES;
        }
        retryPolicy.put("retryableStatusCodes", retryableStatusCodes);

        methodConfig.put("retryPolicy", retryPolicy);
    }

    public void setMaxAttempts(double maxAttempts) {
        if (maxAttempts >= 2) {
            this.maxAttempts = maxAttempts;
        }
    }

    public void setInitialBackOff(long initialBackoff) {
        this.initialBackoff = millisToString(initialBackoff);
    }

    public void setMaxBackoff(long maxBackoff) {
        this.maxBackoff = millisToString(maxBackoff);
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public void setRetryableStatusCodes(List<String> retryableStatusCodes) {
        this.retryableStatusCodes = retryableStatusCodes;
    }

    public String millisToString(long value) {
        return value / 1000.0 + "s";
    }
}
