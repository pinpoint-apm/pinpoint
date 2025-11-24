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

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
class AcceptApplicationLocalCache {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebugEnabled = logger.isDebugEnabled();

    private final ConcurrentMap<RpcApplication, AcceptApplicationSet> acceptApplicationLocalCache = new ConcurrentHashMap<>();

    @Nullable
    public AcceptApplicationSet get(RpcApplication findKey) {
        final AcceptApplicationSet hit = this.acceptApplicationLocalCache.get(findKey);
        if (hit != null) {
            if (isDebugEnabled) {
                logger.debug("acceptApplicationLocalCache hit {}:{}", findKey, hit);
            }
            return hit;
        }
        if (isDebugEnabled) {
            logger.debug("acceptApplicationLocalCache miss {}", findKey);
        }
        return null;
    }

    public void put(RpcApplication findKey, AcceptApplicationSet acceptApplicationSet) {
        if (AcceptApplicationSet.isEmpty(acceptApplicationSet)) {
            // initialize for empty value
            computeIfAbsent(findKey);
            return;
        }
        if (isDebugEnabled) {
            logger.debug("findAcceptApplication:{}", acceptApplicationSet);
        }
        // build cache
        // set AcceptApplication for each url
        for (AcceptApplication acceptApplication : acceptApplicationSet) {
            // acceptApplicationSet data contains the url and the accept node's applicationName.
            // we need to recreate the key set based on the url and the calling application.
            RpcApplication newKey = new RpcApplication(acceptApplication.getHost(), findKey.getApplication());
            AcceptApplicationSet findSet = computeIfAbsent(newKey);
            findSet.add(acceptApplication);
        }
    }

    private AcceptApplicationSet computeIfAbsent(RpcApplication key) {
        return this.acceptApplicationLocalCache.computeIfAbsent(key, rpcApplication -> new AcceptApplicationSet());
    }
}
