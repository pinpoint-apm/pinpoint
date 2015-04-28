/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.web.vo;

import org.springframework.util.Assert;

/**
 * @author emeroad
 */
public class SearchRange {
    private final int callerRange;
    private final int calleeRange;

    public SearchRange(int callerRange, int calleeRange) {
        Assert.isTrue(callerRange >= 0, "negative callerRange");
        Assert.isTrue(calleeRange >= 0, "negative calleeRange");
        this.callerRange = callerRange;
        this.calleeRange = calleeRange;
    }

    public int getCallerRange() {
        return callerRange;
    }

    public int getCalleeRange() {
        return calleeRange;
    }
}
