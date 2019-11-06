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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author HyunGil Jeong
 */
public class EmptyNodeHistogramFactory implements NodeHistogramFactory {

    @Override
    public NodeHistogram createWasNodeHistogram(Application wasApplication, Range range) {
        return new NodeHistogram(wasApplication, range);
    }

    @Override
    public NodeHistogram createTerminalNodeHistogram(Application terminalApplication, Range range, LinkList linkList) {
        return new NodeHistogram(terminalApplication, range);
    }

    @Override
    public NodeHistogram createUserNodeHistogram(Application userApplication, Range range, LinkList linkList) {
        return new NodeHistogram(userApplication, range);
    }

    @Override
    public NodeHistogram createQueueNodeHistogram(Application queueApplication, Range range, LinkList linkList) {
        return new NodeHistogram(queueApplication, range);
    }

    @Override
    public NodeHistogram createEmptyNodeHistogram(Application application, Range range) {
        return new NodeHistogram(application, range);
    }
}
