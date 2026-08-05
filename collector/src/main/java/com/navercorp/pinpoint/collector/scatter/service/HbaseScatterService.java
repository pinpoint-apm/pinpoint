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

import com.navercorp.pinpoint.collector.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HbaseScatterService implements ScatterService {

    private final TraceIndexDao traceIndexDao;

    public HbaseScatterService(TraceIndexDao traceIndexDao) {
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "traceIndexDao");
    }

    @Override
    public void insert(SpanBo span) {
        traceIndexDao.insert(span);
    }
}
