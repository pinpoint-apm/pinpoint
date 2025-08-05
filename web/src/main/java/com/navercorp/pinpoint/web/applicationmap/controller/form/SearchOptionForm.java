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

package com.navercorp.pinpoint.web.applicationmap.controller.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SearchOptionForm {

    public static final int DEFAULT_SEARCH_DEPTH = 1;

    public static final int MIN = DEFAULT_SEARCH_DEPTH;
    public static final int MAX = 4;

    @Min(MIN) @Max(MAX)
    private int callerRange = DEFAULT_SEARCH_DEPTH;

    @Min(MIN) @Max(MAX)
    private int calleeRange = DEFAULT_SEARCH_DEPTH;

    private boolean bidirectional = true;
    private boolean wasOnly = false;

    public int getCallerRange() {
        return callerRange;
    }

    public void setCallerRange(int callerRange) {
        this.callerRange = callerRange;
    }

    public int getCalleeRange() {
        return calleeRange;
    }

    public void setCalleeRange(int calleeRange) {
        this.calleeRange = calleeRange;
    }


    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public boolean isWasOnly() {
        return wasOnly;
    }

    public void setWasOnly(boolean wasOnly) {
        this.wasOnly = wasOnly;
    }

    @Override
    public String toString() {
        return "SearchOptionForm{" +
               "calleeRange=" + calleeRange +
               ", callerRange=" + callerRange +
               ", bidirectional=" + bidirectional +
               ", wasOnly=" + wasOnly +
               '}';
    }
}
