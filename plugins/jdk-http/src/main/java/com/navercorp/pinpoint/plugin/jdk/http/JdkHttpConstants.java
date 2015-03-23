/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jdk.http;

import static com.navercorp.pinpoint.common.HistogramSchema.*;

import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author Jongho Moon
 *
 */
public interface JdkHttpConstants {
    public static final ServiceType SERVICE_TYPE = ServiceType.of(9055, "JDK_HTTPURLCONNECTOR", "JDK_HTTPCONNECTOR", NORMAL_SCHEMA);
}
