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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;

import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class AnnotationRecord extends BaseRecord {
    public AnnotationRecord(final int tab, final int id, final int parentId, final String title, final String arguments, final boolean authorized) {
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.arguments = arguments;
        this.isAuthorized = authorized;
    }
}