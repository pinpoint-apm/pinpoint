package com.nhn.pinpoint.profiler.modifier.db;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author emeroad
 */
public interface ConnectionStringParser {
    DatabaseInfo parse(String url);
}
