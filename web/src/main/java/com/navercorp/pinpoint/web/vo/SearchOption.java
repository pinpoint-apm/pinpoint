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

import com.navercorp.pinpoint.web.service.map.LinkSelectorType;
import org.springframework.util.Assert;

/**
 * @author emeroad
 */
public class SearchOption {
    private final int callerSearchDepth;
    private final int calleeSearchDepth;
    private final LinkSelectorType linkSelectorType;
    private final boolean wasOnly;

    public SearchOption(int callerSearchDepth, int calleeSearchDepth) {
        this(callerSearchDepth, calleeSearchDepth, false, false);
    }

    public SearchOption(int callerSearchDepth, int calleeSearchDepth, boolean bidirectional, boolean wasOnly) {
        this(callerSearchDepth, calleeSearchDepth, bidirectional ? LinkSelectorType.BIDIRECTIONAL : LinkSelectorType.UNIDIRECTIONAL, wasOnly);
    }

    public SearchOption(int callerSearchDepth, int calleeSearchDepth, LinkSelectorType linkSelectorType, boolean wasOnly) {
        Assert.isTrue(callerSearchDepth >= 0, "negative callerSearchDepth");
        Assert.isTrue(calleeSearchDepth >= 0, "negative calleeSearchDepth");
        this.callerSearchDepth = callerSearchDepth;
        this.calleeSearchDepth = calleeSearchDepth;
        this.linkSelectorType = linkSelectorType;
        this.wasOnly = wasOnly;
    }

    public int getCallerSearchDepth() {
        return callerSearchDepth;
    }

    public int getCalleeSearchDepth() {
        return calleeSearchDepth;
    }

    public LinkSelectorType getLinkSelectorType() {
        return linkSelectorType;
    }

    public boolean isWasOnly() {
        return wasOnly;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchOption{");
        sb.append("callerSearchDepth=").append(callerSearchDepth);
        sb.append(", calleeSearchDepth=").append(calleeSearchDepth);
        sb.append(", linkSelectorType=").append(linkSelectorType);
        sb.append(", wasOnly=").append(wasOnly);
        sb.append('}');
        return sb.toString();
    }
}
