/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.dao;

import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author intr3p1d
 */
public interface InboundDao extends CachedStatisticsDao {
    // src -> dest
    // inbound (rowKey dest <- columnName src)
    // outbound (rowKey src -> columnName dest)
    void update(
            String srcServiceName, String srcApplicationName, ServiceType srcApplicationType,
            String destServiceName, String destApplicationName, ServiceType destApplicationType,
            String srcHost, int elapsed, boolean isError
    );
}
