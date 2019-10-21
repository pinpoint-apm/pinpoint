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

import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;

/**
 * @author emeroad
 */
public class LinkStateResolver {
    public static final LinkStateResolver DEFAULT_LINK_STATE_RESOLVER = new LinkStateResolver();
    public static final String BAD = "bad";

    public String resolve(Link link) {
        if (link == null) {
            throw new NullPointerException("link");
        }
        // since Histogram dup gets created, we simply accepts as a parameter
        // XXX need to fix this 
        final long error = getErrorRate(link.getHistogram());
        if (error * 100 > 10) {
            return BAD;
        }
        return "default";

    }

    public boolean isAlert(Link link) {
        String resolve = resolve(link);
        if (BAD.equals(resolve)) {
            return true;
        }
        return false;
    }

    private long getErrorRate(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram");
        }
        final long totalCount = histogram.getTotalCount();
        if (totalCount == 0) {
            return 0;
        }
        return histogram.getTotalErrorCount() / totalCount;
    }
}
