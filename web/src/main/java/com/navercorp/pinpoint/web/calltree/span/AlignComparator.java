/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventComparator;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class AlignComparator implements Comparator<Align> {

    public static final AlignComparator INSTANCE = new AlignComparator();

    @Override
    public int compare(Align a1, Align a2) {
        final SpanEventBo event1 = a1.getSpanEventBo();
        final SpanEventBo event2 = a2.getSpanEventBo();
        return SpanEventComparator.INSTANCE.compare(event1, event2);
    }

}
