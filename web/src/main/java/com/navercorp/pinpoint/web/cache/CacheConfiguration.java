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

package com.navercorp.pinpoint.web.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.common.server.config.CommonCacheManagerConfiguration;
import com.navercorp.pinpoint.common.server.config.CustomCacheRegistration;
import com.navercorp.pinpoint.common.server.config.DefaultCustomCacheRegistration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Import(CommonCacheManagerConfiguration.class)
public class CacheConfiguration implements CachingConfigurer {

    public static final String API_METADATA_CACHE_NAME = "apiMetaData";
    public static final String APPLICATION_LIST_CACHE_NAME = "applicationNameList";

    @Bean
    public CacheManager apiMetaData() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(API_METADATA_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(500)
                .maximumSize(10000));
        return caffeineCacheManager;
    }

    @Bean
    public CustomCacheRegistration apiMetadataCache() {
        Cache<Object, Object> cache = Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(500)
                .maximumSize(10000)
                .build();
        return new DefaultCustomCacheRegistration(API_METADATA_CACHE_NAME, cache);
    }

    @Bean
    public CacheManager applicationNameList() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(APPLICATION_LIST_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200));
        return caffeineCacheManager;
    }

    @Bean
    public CustomCacheRegistration applicationNameListCache() {
        Cache<Object, Object> cache = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200)
                .build();
        return new DefaultCustomCacheRegistration(APPLICATION_LIST_CACHE_NAME, cache);
    }

}
