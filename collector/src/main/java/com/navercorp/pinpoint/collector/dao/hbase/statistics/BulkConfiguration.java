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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class BulkConfiguration {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final int callerLimitSize;

    private final int calleeLimitSize;

    private final int selfLimitSize;

    private final boolean enableBulk;


    public BulkConfiguration(@Value("${collector.cachedStatDao.caller.limit:-1}") int callerLimitSize,
                             @Value("${collector.cachedStatDao.callee.limit:-1}") int calleeLimitSize,
                             @Value("${collector.cachedStatDao.self.limit:-1}") int selfLimitSize,
                             @Value("${collector.cachedStatDao.bulk.enable:true}") boolean enableBulk) {
        this.callerLimitSize = callerLimitSize;
        this.calleeLimitSize = calleeLimitSize;
        this.selfLimitSize = selfLimitSize;
        this.enableBulk = enableBulk;
    }

    public int getCallerLimitSize() {
        return callerLimitSize;
    }

    public int getCalleeLimitSize() {
        return calleeLimitSize;
    }

    public int getSelfLimitSize() {
        return selfLimitSize;
    }

    public boolean enableBulk() {
        return enableBulk;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
    }

    @Override
    public String toString() {
        return "BulkConfiguration{" +
                "callerLimitSize=" + callerLimitSize +
                ", calleeLimitSize=" + calleeLimitSize +
                ", selfLimitSize=" + selfLimitSize +
                ", enableBulk=" + enableBulk +
                '}';
    }
}
