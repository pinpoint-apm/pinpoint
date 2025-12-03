/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.scatter.service;

import com.navercorp.pinpoint.collector.scatter.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HbaseScatterService implements ScatterService {

    private final ApplicationTraceIndexDao applicationTraceIndexDao;
    private final ApplicationTraceIndexDao applicationTraceIndexDaoV2;
    private final boolean enableApplicationTraceIndexV1;
    private final boolean enableApplicationTraceIndexV2;

    public HbaseScatterService(
            ApplicationTraceIndexDao applicationTraceIndexDao,
            @Qualifier("hbaseApplicationTraceIndexDaoV2")
            ApplicationTraceIndexDao applicationTraceIndexDaoV2,
            @Value("${pinpoint.collector.application.trace.index.v1.enabled:true}")
            boolean enableApplicationTraceIndexV1,
            @Value("${pinpoint.collector.application.trace.index.v2.enabled:false}")
            boolean enableApplicationTraceIndexV2
    ) {
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.applicationTraceIndexDaoV2 = Objects.requireNonNull(applicationTraceIndexDaoV2, "applicationTraceIndexDaoV2");
        this.enableApplicationTraceIndexV1 = enableApplicationTraceIndexV1;
        this.enableApplicationTraceIndexV2 = enableApplicationTraceIndexV2;
    }

    @Override
    public void insert(SpanBo span) {
        if (enableApplicationTraceIndexV1) {
            applicationTraceIndexDao.insert(span);
        }
        if (enableApplicationTraceIndexV2) {
            applicationTraceIndexDaoV2.insert(span);
        }
    }
}
