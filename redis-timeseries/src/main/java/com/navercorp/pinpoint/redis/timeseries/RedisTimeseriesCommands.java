package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;

import java.util.List;

public interface RedisTimeseriesCommands extends AutoCloseable {


    long tsAdd(String key, long timestamp, double value);

    long tsAdd(String key, long timestamp, double value, TsAddArgs addOptions);


    /**
     * Delete data in the range of fromTimestamp to toTimestamp.
     * @param key key
     * @param fromTimestamp fromTimestamp
     * @param toTimestamp toTimestamp
     * @return timestamp
     */
    long tsDel(String key, long fromTimestamp, long toTimestamp);

    List<TimestampValuePair> tsRange(String key, long fromTimestamp, long toTimestamp);

    TimestampValuePair tsGet(String key);

    List<TimestampValuePair> tsRevrange(String key, long fromTimestamp, long toTimestamp);

    void close();
}
