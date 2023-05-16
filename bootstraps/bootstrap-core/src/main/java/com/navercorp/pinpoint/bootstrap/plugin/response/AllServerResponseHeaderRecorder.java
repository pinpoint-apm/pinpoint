/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.response;

import com.navercorp.pinpoint.bootstrap.context.AttributeRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author yjqg6666
 */
public class AllServerResponseHeaderRecorder<RESP> implements ServerResponseHeaderRecorder<RESP> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    public static final String HEADERS_ALL = "HEADERS-ALL";

    private final ResponseAdaptor<RESP> responseAdaptor;

    public AllServerResponseHeaderRecorder(ResponseAdaptor<RESP> responseAdaptor) {
        this.responseAdaptor = Objects.requireNonNull(responseAdaptor, "responseAdaptor");
    }

    public static boolean isRecordAllHeaders(List<String> recordHeaders) {
        return recordHeaders.contains(HEADERS_ALL);
    }

    @Override
    public void recordHeader(final AttributeRecorder recorder, final RESP response) {
        for (String headerName : getHeaderNames(response)) {
            if (StringUtils.isEmpty(headerName)) {
                continue;
            }
            final Collection<String> headers = responseAdaptor.getHeaderNames(response);
            if (CollectionUtils.isEmpty(headers)) {
                continue;
            }
            StringStringValue header = new StringStringValue(headerName, formatHeaderValues(headers));
            recorder.recordAttribute(AnnotationKey.HTTP_RESPONSE_HEADER, header);
        }
    }

    private Set<String> getHeaderNames(final RESP response) {
        final Collection<String> headerNames = responseAdaptor.getHeaderNames(response);
        if (CollectionUtils.isEmpty(headerNames)) {
            return Collections.emptySet();
        }
        //deduplicate
        Set<String> names = new HashSet<>(headerNames.size());
        names.addAll(headerNames);
        return names;

    }

    private String formatHeaderValues(Collection<String> headers) {
        if (headers.size() == 1) {
            return headers.iterator().next();
        } else {
            return headers.toString();
        }
    }
}
