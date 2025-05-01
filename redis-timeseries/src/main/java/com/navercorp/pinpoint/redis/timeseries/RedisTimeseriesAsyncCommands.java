package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import io.lettuce.core.RedisFuture;

import java.util.List;

public interface RedisTimeseriesAsyncCommands {


    RedisFuture<Long> tsAdd(String key, long timestamp, double value);

    RedisFuture<Long> tsAdd(String key, long timestamp, double value, TsAddArgs addOptions);


    /**
     * Delete data in the range of fromTimestamp to toTimestamp.
     * @param key key
     * @param fromTimestamp fromTimestamp
     * @param toTimestamp toTimestamp
     * @return timestamp
     */
    RedisFuture<Long> tsDel(String key, long fromTimestamp, long toTimestamp);

    RedisFuture<List<TimestampValuePair>> tsRange(String key, long fromTimestamp, long toTimestamp);

    RedisFuture<List<TimestampValuePair>> tsRange(String key, String... args);
//
    RedisFuture<TimestampValuePair> tsGet(String key);
//
    RedisFuture<List<TimestampValuePair>> tsRevrange(String key, long fromTimestamp, long toTimestamp);

}
