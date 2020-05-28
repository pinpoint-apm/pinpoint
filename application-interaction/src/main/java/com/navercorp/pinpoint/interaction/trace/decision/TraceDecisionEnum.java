/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.interaction.trace.decision;

/**
 * @author yjqg6666
 */
@SuppressWarnings("unused")
public enum TraceDecisionEnum {

    /**
     * do not trace
     */
    NoTrace(),

    /**
     * force trace unless excluded
     */
    ForceTrace(),

    /**
     * trace depends on sampling rate config
     */
    RateTrace();

    public boolean noTrace() {
        return isEqual(NoTrace);
    }
    public boolean forceTrace() {
        return isEqual(ForceTrace);
    }
    public boolean rateTrace() {
        return isEqual(RateTrace);
    }

    private boolean isEqual(TraceDecisionEnum check) {
        return check != null && this.name().equalsIgnoreCase(check.name());
    }

}
