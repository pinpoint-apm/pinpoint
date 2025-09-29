/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * <pre>
 * columnName format = SERVICETYPE(2bytes) + SLOT(2bytes) + APPNAMELEN(2bytes) + APPLICATIONNAME(str) + HOST(str)
 * </pre>
 *
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class MapSlotUtils {
    private MapSlotUtils() {
    }


    public static short getSlotNumber(ServiceType serviceType, int elapsed, boolean isError) {
        return getHistogramSlot(serviceType, elapsed, isError).getSlotTime();
    }

    public static HistogramSlot getHistogramSlot(ServiceType serviceType, int elapsed, boolean isError) {
        Objects.requireNonNull(serviceType, "serviceType");

        final HistogramSchema histogramSchema = serviceType.getHistogramSchema();
        return histogramSchema.findHistogramSlot(elapsed, isError);
    }

    public static short getPingSlotNumber(ServiceType serviceType) {
        return getPingSlot(serviceType).getSlotTime();
    }

    public static HistogramSlot getPingSlot(ServiceType serviceType) {
        final HistogramSchema histogramSchema = serviceType.getHistogramSchema();
        return histogramSchema.getPingSlot();
    }

}
