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
package com.navercorp.pinpoint.web.security;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;

/**
 * @author minwoo.jung
 */
public interface MetaDataFilter {
    
    public enum MetaData {
        API, SQL, PARAM
    }

    boolean filter(SpanAlign spanAlign, MetaData metaData);

    AnnotationBo createAnnotationBo(SpanAlign spanAlign, MetaData metaData);

    Record createRecord(CallTreeNode node, RecordFactory factory);

    void replaceAnnotationBo(SpanAlign align, MetaData param);
}
