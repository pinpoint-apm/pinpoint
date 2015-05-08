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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanAligner2;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanResult {
    private int completeType;
    private CallTreeIterator callTreeIterator;

    public SpanResult(int completeType, CallTreeIterator callTreeIterator) {
        if (callTreeIterator == null) {
            throw new NullPointerException("spanAlignList must not be null");
        }
        this.completeType = completeType;
        this.callTreeIterator = callTreeIterator;
    }

    public int getCompleteType() {
        return completeType;
    }

    public CallTreeIterator getCallTree() {
        return callTreeIterator;
    }

    public String getCompleteTypeString() {
        switch (completeType) {
            case SpanAligner2.BEST_MATCH:
                return "Complete";
            case SpanAligner2.START_TIME_MATCH:
                return "Progress";
            case SpanAligner2.FAIL_MATCH:
                return "Error";
        }
        return "Error";
    }
}
