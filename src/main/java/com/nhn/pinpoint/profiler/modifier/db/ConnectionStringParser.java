package com.nhn.pinpoint.profiler.modifier.db;

import com.nhn.pinpoint.profiler.context.DatabaseInfo;

/**
 * @author emeroad
 */
public interface ConnectionStringParser {
    DatabaseInfo parse(String url);
}
