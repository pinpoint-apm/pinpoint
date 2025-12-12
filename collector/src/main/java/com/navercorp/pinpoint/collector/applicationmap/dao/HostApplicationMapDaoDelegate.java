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

package com.navercorp.pinpoint.collector.applicationmap.dao;

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class HostApplicationMapDaoDelegate implements HostApplicationMapDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HostApplicationMapDao[] delegates;

    public HostApplicationMapDaoDelegate(HostApplicationMapDao[] delegates) {
        this.delegates = Objects.requireNonNull(delegates, "delegates");
    }

    @Override
    public void insert(long requestTime, String parentApplicationName, int parentServiceType, Vertex selfVertex, String host) {
        for (HostApplicationMapDao delegate : delegates) {
            try {
                delegate.insert(requestTime, parentApplicationName, parentServiceType, selfVertex, host);
            } catch (Exception e) {
                logger.warn("Failed to insert host application map. parentApplicationName:{}, parentServiceType:{}, selfVertex:{}, host:{}", parentApplicationName, parentServiceType, selfVertex, host, e);
            }
        }}
}
