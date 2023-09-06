/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.web.applicationmap.map.AcceptApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
class AcceptApplicationLocalCache {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<RpcApplication, Set<AcceptApplication>> acceptApplicationLocalCache = new ConcurrentHashMap<>();

    public Set<AcceptApplication> get(RpcApplication findKey) {
        final Set<AcceptApplication> hit = this.acceptApplicationLocalCache.get(findKey);
        if (hit != null) {
            logger.debug("acceptApplicationLocalCache hit {}:{}", findKey, hit);
            return hit;
        }
        logger.debug("acceptApplicationLocalCache miss {}", findKey);
        return Collections.emptySet();
    }

    public void put(RpcApplication findKey, Set<AcceptApplication> acceptApplicationSet) {
        if (CollectionUtils.isEmpty(acceptApplicationSet)) {
            // initialize for empty value
            computeIfAbsent(findKey);
            return;
        }
        logger.debug("findAcceptApplication:{}", acceptApplicationSet);
        // build cache
        // set AcceptApplication for each url
        for (AcceptApplication acceptApplication : acceptApplicationSet) {
            // acceptApplicationSet data contains the url and the accept node's applicationName.
            // we need to recreate the key set based on the url and the calling application.
            RpcApplication newKey = new RpcApplication(acceptApplication.getHost(), findKey.getApplication());
            Set<AcceptApplication> findSet = computeIfAbsent(newKey);
            findSet.add(acceptApplication);
        }
    }

    private Set<AcceptApplication> computeIfAbsent(RpcApplication key) {
        return this.acceptApplicationLocalCache.computeIfAbsent(key, rpcApplication -> ConcurrentHashMap.newKeySet());
    }
}
