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
package com.navercorp.pinpoint.servermap.dao.redis;

import com.navercorp.pinpoint.redis.timeseries.RedisTimeseriesAsyncCommands;
import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import com.navercorp.pinpoint.servermap.bo.CallCount;
import com.navercorp.pinpoint.servermap.bo.DirectionalBo;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@Repository
public class RedisDao {

    // "ts:ApplicationMapInbound:tenantId:default:test-local-SY:32765:default:test-local-SY:32765"
    private final RedisCommands<String, String> redisCommands;
    private final RedisTimeseriesAsyncCommands redisTimeseriesAsyncCommands;
    private final RedisCodec<String, String> commandCodec = StringCodec.ASCII;

    private final RedisCodec<String, String> outputCodec = StringCodec.UTF8;
    private Logger logger = LogManager.getLogger(this.getClass());

    public RedisDao(
            RedisCommands<String, String> redisCommands,
            RedisTimeseriesAsyncCommands redisTimeseriesAsyncCommands
    ) {
        this.redisCommands = Objects.requireNonNull(redisCommands, "redisCommands");
        this.redisTimeseriesAsyncCommands = Objects.requireNonNull(redisTimeseriesAsyncCommands, "redisTimeseriesAsyncCommands");
    }

    // 127.0.0.1:6379> TS.RANGE ts:ApplicationMapInbound:tenantId:default:test-local-SY:default:test-local-SY:32765 - + AGGREGATION sum 60000

    public List<DirectionalBo> readData() {
        List<String> keys = getKeys();
        List<DirectionalBo> bos = new ArrayList<>();
        for (String key : keys) {
            logger.info("Key: {}", key);
            RedisFuture<List<TimestampValuePair>> future = redisTimeseriesAsyncCommands.tsRange(
                    key, "-", "+", "AGGREGATION", "sum", "60000"
            );
            try {
                List<TimestampValuePair> pairs = future.get();
                bos.add(toDirectionalBo(key, pairs));
            } catch (Exception e) {
                logger.error("Error reading data", e);
            }
        }
        return bos;
    }

    public DirectionalBo toDirectionalBo(String key, List<TimestampValuePair> pairs) {
        DirectionalBo bo = DirectionalBo.fromKey(key);
        List<CallCount> callCounts = new ArrayList<>();
        for (TimestampValuePair pair : pairs) {
            callCounts.add(new CallCount(pair.timestamp(), (long) pair.value()));
        }
        bo.setCallCountList(callCounts);

        return bo;
    }


    public List<String> getKeys() {
        String pattern = "ts:*";
        ScanCursor cursor = new ScanCursor();
        cursor.setCursor("0");
        logger.info("SCAN -> {}", cursor.getCursor());
        KeyScanCursor<String> result;
        List<String> keys = new ArrayList<>();
        do {
            result = getKeys(cursor, cursor.getCursor(), 20, pattern);
            int i = 1;
            for (String key : result.getKeys()) {
                logger.info("  {} ) {}", i++, key);
            }

            logger.info("Next cursor: {}", result.getCursor());
            cursor.setCursor(result.getCursor());

            keys.addAll(result.getKeys());
        } while (!result.isFinished());
        return keys;
    }


    public KeyScanCursor<String> getKeys(ScanCursor cursor, String startAt, int count, String pattern) {
        cursor.setCursor(startAt);
        ScanArgs args = ScanArgs.Builder.limit(count).match(pattern);
        return redisCommands.scan(cursor, args);
    }

}
