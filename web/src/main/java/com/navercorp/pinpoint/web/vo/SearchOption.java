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

import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorType;
import org.springframework.util.Assert;

/**
 * @author emeroad
 */
public class SearchOption {
    private final int outSearchDepth;
    private final int inSearchDepth;
    private final LinkSelectorType linkSelectorType;
    private final boolean wasOnly;

    public SearchOption(int outSearchDepth, int inSearchDepth) {
        this(outSearchDepth, inSearchDepth, false, false);
    }

    public SearchOption(int outSearchDepth, int inSearchDepth, boolean bidirectional, boolean wasOnly) {
        this(outSearchDepth, inSearchDepth, bidirectional ? LinkSelectorType.BIDIRECTIONAL : LinkSelectorType.UNIDIRECTIONAL, wasOnly);
    }

    public SearchOption(int outSearchDepth, int inSearchDepth, LinkSelectorType linkSelectorType, boolean wasOnly) {
        Assert.isTrue(outSearchDepth >= 0, "negative outSearchDepth");
        Assert.isTrue(inSearchDepth >= 0, "negative inSearchDepth");
        this.outSearchDepth = outSearchDepth;
        this.inSearchDepth = inSearchDepth;
        this.linkSelectorType = linkSelectorType;
        this.wasOnly = wasOnly;
    }

    public int getOutSearchDepth() {
        return outSearchDepth;
    }

    public int getInSearchDepth() {
        return inSearchDepth;
    }

    public LinkSelectorType getLinkSelectorType() {
        return linkSelectorType;
    }

    public boolean isWasOnly() {
        return wasOnly;
    }

    @Override
    public String toString() {
        return "SearchOption{" +
                "outSearchDepth=" + outSearchDepth +
                ", inSearchDepth=" + inSearchDepth +
                ", linkSelectorType=" + linkSelectorType +
                ", wasOnly=" + wasOnly +
                '}';
    }
}
