/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class LinkHistogramSummary {

    private final Link link;

    public LinkHistogramSummary(Link link) {
        this.link = Objects.requireNonNull(link, "link");
    }

    public LinkName getLinkName() {
        return link.getLinkName();
    }

    public Histogram getHistogram() {
        return link.getHistogram();
    }

    public ApplicationTimeHistogram getLinkApplicationTimeHistogram() {
        return link.getLinkApplicationTimeHistogram();
    }

    @Override
    public String toString() {
        return "LinkHistogramSummary{" +
                "link=" + link +
                '}';
    }
}
