/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * @author yjqg6666
 */
@Service
public class CacheServiceImpl implements CacheService {

    private final EhCacheCacheManager cacheManager;

    public CacheServiceImpl(EhCacheCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public String getApplicationListETag() {
        final long cacheUpdateCount = this.getApplicationListUpdateCount();

        return cacheUpdateCount > 0 ? APPLICATION_LIST_CACHE_NAME + "Ver" + cacheUpdateCount : UUID.randomUUID().toString();
    }

    public void clearApplicationListCache() {
        getApplicationListCache().ifPresent(Ehcache::removeAll);
    }

    private long getApplicationListUpdateCount() {
        return getApplicationListCache()
                .map(ehcache -> ehcache.getStatistics().cachePutCount())
                .orElse(0L);
    }

    private Optional<Ehcache> getApplicationListCache() {
        final CacheManager cacheManager = this.cacheManager.getCacheManager();
        if (cacheManager != null) {
            return Optional.ofNullable(cacheManager.getEhcache(APPLICATION_LIST_CACHE_NAME));
        }
        return Optional.empty();
    }
}
