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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HedgingServiceConfigBuilder implements ServiceConfigBuilder {

    public static final int DEFAULT_MAX_ATTEMPTS = 3;
    public static final long DEFAULT_HEDGING_DELAY_MILLIS = 1000L;

    private double maxAttempts = DEFAULT_MAX_ATTEMPTS; //Required. Must be two or greater
    private String hedgingDelay = millisToString(DEFAULT_HEDGING_DELAY_MILLIS);  //Required. Long decimal with "s" appended
    private List<String> nonFatalStatusCodes; //Optional (eg. [14], ["UNAVAILABLE"] or ["unavailable"])

    @Override
    public Map<String, ?> buildMetadataConfig() {
        Map<String, Object> methodConfig = new LinkedHashMap<>();
        addMetadataService(methodConfig);
        addHedgingPolicy(methodConfig);
        return Collections.singletonMap("methodConfig", Collections.singletonList(methodConfig));
    }

    private void addMetadataService(Map<String, Object> methodConfig) {
        Map<String, Object> service = Collections.singletonMap("service", METADATA_SERVICE);
        methodConfig.put("name", Collections.singletonList(service));
    }

    private void addHedgingPolicy(Map<String, Object> methodConfig) {
        Map<String, Object> retryPolicy = new LinkedHashMap<>();
        retryPolicy.put("maxAttempts", maxAttempts);
        retryPolicy.put("hedgingDelay", hedgingDelay);
        if (nonFatalStatusCodes != null && !nonFatalStatusCodes.isEmpty()) {
            retryPolicy.put("nonFatalStatusCodes", nonFatalStatusCodes);
        }

        methodConfig.put("hedgingPolicy", retryPolicy);
    }


    public void setMaxAttempts(int maxAttempts) {
        if (maxAttempts >= 2) {
            this.maxAttempts = maxAttempts;
        }
    }

    public void setHedgingDelayMillis(long hedgingDelay) {
        this.hedgingDelay = millisToString(hedgingDelay);
    }

    public void setNonFatalStatusCodes(List<String> nonFatalStatusCodes) {
        this.nonFatalStatusCodes = nonFatalStatusCodes;
    }

    public String millisToString(long value) {
        return value / 1000.0 + "s";
    }
}
