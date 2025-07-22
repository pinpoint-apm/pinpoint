/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.OneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RangeOneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributorConfiguration {

    private final Logger logger = LogManager.getLogger(DistributorConfiguration.class);

    public DistributorConfiguration() {
        logger.info("Install {}", DistributorConfiguration.class.getSimpleName());
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationTraceIndexDistributor() {
        ByteHasher hasher = newOneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private ByteHasher newOneByteSimpleHash(int maxBuckets) {
        return new OneByteSimpleHash(maxBuckets);
    }

    @Bean
    public RowKeyDistributorByHashPrefix traceV2Distributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(32, 40, 256);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor2() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix acceptApplicationRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 24, 4);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapInLinkRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapOutLinkRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapSelfRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private ByteHasher newRangeOneByteSimpleHash(int start, int end, int maxBuckets) {
        return new RangeOneByteSimpleHash(start, end, maxBuckets);
    }

}
