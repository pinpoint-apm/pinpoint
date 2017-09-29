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

package com.navercorp.pinpoint.web.service.map;

import com.google.common.collect.Sets;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
public class ParallelApplicationsMapCreator implements ApplicationsMapCreator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationMapCreator applicationMapCreator;

    private final ExecutorService executorService;

    public ParallelApplicationsMapCreator(ApplicationMapCreator applicationMapCreator, ExecutorService executorService) {
        if (applicationMapCreator == null) {
            throw new NullPointerException("applicationMapCreator must not be null");
        }
        if (executorService == null) {
            throw new NullPointerException("executorService must not be null");
        }
        this.applicationMapCreator = applicationMapCreator;
        this.executorService = executorService;
    }

    @Override
    public LinkDataDuplexMap createLinkDataDuplexMap(List<Application> applications, LinkSelectContext linkSelectContext) {
        final Set<LinkDataDuplexMap> searchResults = Sets.newConcurrentHashSet();
        CompletableFuture[] futures = getLinkDataMapFutures(searchResults, applications, linkSelectContext);
        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            logger.error("Error selecting link", e);
            return new LinkDataDuplexMap();
        }
        LinkDataDuplexMap resultMap = new LinkDataDuplexMap();
        for (LinkDataDuplexMap searchResult : searchResults) {
            resultMap.addLinkDataDuplexMap(searchResult);
        }
        return resultMap;
    }

    private CompletableFuture[] getLinkDataMapFutures(Set<LinkDataDuplexMap> searchResults, List<Application> targetApplicationList, LinkSelectContext linkSelectContext) {
        List<CompletableFuture<Void>> linkDataDuplexMapFutures = new ArrayList<>();
        for (Application targetApplication : targetApplicationList) {
            CompletableFuture<LinkDataDuplexMap> linkDataDuplexMapFuture = CompletableFuture.supplyAsync(new Supplier<LinkDataDuplexMap>() {
                @Override
                public LinkDataDuplexMap get() {
                    return applicationMapCreator.createMap(targetApplication, linkSelectContext);
                }
            }, executorService);
            CompletableFuture<Void> searchResultsFuture = linkDataDuplexMapFuture.thenAccept(searchResults::add);
            linkDataDuplexMapFutures.add(searchResultsFuture);
        }
        return linkDataDuplexMapFutures.toArray(new CompletableFuture[linkDataDuplexMapFutures.size()]);
    }
}
