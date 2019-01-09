/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;

/**
 * @author lopiter
 */
public class AkkaHttpConstants {

    public static final ServiceType AKKA_HTTP_SERVER = ServiceTypeFactory.of(1310, "AKKA_HTTP_SERVER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType AKKA_HTTP_SERVER_INTERNAL = ServiceTypeFactory.of(9998, "1311", "AKKA_HTTP_SERVER_INTERNAL");

}
