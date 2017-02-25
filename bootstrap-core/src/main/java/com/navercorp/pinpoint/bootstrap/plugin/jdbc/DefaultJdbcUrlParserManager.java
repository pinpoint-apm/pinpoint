/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Taejin Koo
 */
public class DefaultJdbcUrlParserManager implements JdbcUrlParserManager {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebugEnabled = logger.isDebugEnabled();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private final ConcurrentMap<String, JdbcUrlParsingResult> successCache = new ConcurrentHashMap<String, JdbcUrlParsingResult>();
    private final ConcurrentMap<FailCacheKey, JdbcUrlParsingResult> failCache = new ConcurrentHashMap<FailCacheKey, JdbcUrlParsingResult>();
    private final ConcurrentHashMap<ServiceType, JdbcUrlParser> jdbcUrlParserMap = new ConcurrentHashMap<ServiceType, JdbcUrlParser>();

    // The DriverConnectInterceptor is called later than the DataSource.
    // After the new DriveConnectInterceptor is called, the cache must be cleared.
    @Override
    public boolean addJdbcUrlParser(JdbcUrlParser jdbcUrlParser) {
        if (jdbcUrlParser == null) {
            throw new NullPointerException("jdbcUrlParser may not be null");
        }

        writeLock.lock();
        try {
            ServiceType serviceType = jdbcUrlParser.getServiceType();
            JdbcUrlParser oldJdbcUrlParser = jdbcUrlParserMap.putIfAbsent(serviceType, jdbcUrlParser);
            if (oldJdbcUrlParser == null) {
                failCache.clear();
                return true;
            } else {
                return false;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public DatabaseInfo parse(String url) {
        return parseWithResult(url).getDatabaseInfo();
    }

    @Override
    public DatabaseInfo parse(ServiceType serviceType, String url) {
        return parseWithResult(serviceType, url).getDatabaseInfo();
    }

    // http://www.petefreitag.com/articles/jdbc_urls/
    @Override
    public JdbcUrlParsingResult parseWithResult(String url) {
        return parseWithResult(null, url);
    }

    @Override
    public JdbcUrlParsingResult parseWithResult(ServiceType serviceType, String jdbcUrl) {
        if (jdbcUrl == null) {
            return new JdbcUrlParsingResult(false, UnKnownDatabaseInfo.INSTANCE);
        }

        final JdbcUrlParsingResult successHit = successCache.get(jdbcUrl);
        if (successHit != null) {
            if (isDebugEnabled) {
                logger.debug("database url cache hit:{} {}", jdbcUrl, successHit);
            }
            return successHit;
        }

        readLock.lock();
        try {
            FailCacheKey failCacheKey = new FailCacheKey(serviceType, jdbcUrl);
            final JdbcUrlParsingResult failHit = failCache.get(failCacheKey);
            if (failHit != null) {
                if (isDebugEnabled) {
                    logger.debug("database url fail-cache hit:{} {}", jdbcUrl, failHit);
                }
                return failHit;
            }

            List<JdbcUrlParser> jdbcUrlParserList = findJdbcUrlParser(serviceType, jdbcUrl);
            JdbcUrlParsingResult parsingResult = getDatabaseInfo(jdbcUrl, jdbcUrlParserList);

            if (parsingResult.isSuccess()) {
                final JdbcUrlParsingResult old = successCache.putIfAbsent(jdbcUrl, parsingResult);
                if (old != null) {
                    return old;
                }
            } else {
                final JdbcUrlParsingResult old = failCache.putIfAbsent(failCacheKey, parsingResult);
                if (old != null) {
                    return old;
                }
            }

            return parsingResult;
        } finally {
            readLock.unlock();
        }
    }

    private List<JdbcUrlParser> findJdbcUrlParser(ServiceType serviceType, String jdbcUrl) {
        if (serviceType == null) {
            return findJdbcUrlParser(jdbcUrl);
        } else {
            return findJdbcUrlParser(serviceType);
        }
    }

    private List<JdbcUrlParser> findJdbcUrlParser(ServiceType serviceType) {
        JdbcUrlParser jdbcUrlParser = jdbcUrlParserMap.get(serviceType);
        if (jdbcUrlParser == null) {
            return Arrays.asList(UnknownJdbcUrlParser.INSTANCE);
        }
        return Arrays.asList(jdbcUrlParser);
    }

    private List<JdbcUrlParser> findJdbcUrlParser(String jdbcUrl) {
        if (jdbcUrl == null) {
            return Arrays.asList(UnknownJdbcUrlParser.INSTANCE);
        }

        List<JdbcUrlParser> result = new ArrayList<JdbcUrlParser>(1);
        for (JdbcUrlParser jdbcUrlParser : jdbcUrlParserMap.values()) {
            if (jdbcUrlParser.isPrefixMatch(jdbcUrl)) {
                result.add(jdbcUrlParser);
            }
        }

        if (result.size() > 0) {
            return result;
        } else {
            return Arrays.asList(UnknownJdbcUrlParser.INSTANCE);
        }
    }

    private JdbcUrlParsingResult getDatabaseInfo(String jdbcUrl, List<JdbcUrlParser> jdbcUrlParserList) {
        if (CollectionUtils.nullSafeSize(jdbcUrlParserList) == 0) {
            return new JdbcUrlParsingResult(false, UnKnownDatabaseInfo.INSTANCE);
        } else {
            for (JdbcUrlParser jdbcUrlParser : jdbcUrlParserList) {
                try {
                    JdbcUrlParsingResult result = jdbcUrlParser.parse(jdbcUrl);
                    if (result.isSuccess()) {
                        return result;
                    }
                } catch (Exception e) {
                    logger.error("connectionString parse fail. url:{}, parser:{} ", jdbcUrl, jdbcUrlParser);
                }
            }
        }

        return new JdbcUrlParsingResult(false, UnKnownDatabaseInfo.INSTANCE);
    }

    private static class FailCacheKey {

        private final String jdbcUrl;
        private final ServiceType serviceType;

        public FailCacheKey(ServiceType serviceType, String jdbcUrl) {
            this.serviceType = serviceType;
            this.jdbcUrl = jdbcUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FailCacheKey that = (FailCacheKey) o;

            if (jdbcUrl != null ? !jdbcUrl.equals(that.jdbcUrl) : that.jdbcUrl != null) return false;
            return serviceType != null ? serviceType.equals(that.serviceType) : that.serviceType == null;
        }

        @Override
        public int hashCode() {
            int result = jdbcUrl != null ? jdbcUrl.hashCode() : 0;
            result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
            return result;
        }

    }

}
