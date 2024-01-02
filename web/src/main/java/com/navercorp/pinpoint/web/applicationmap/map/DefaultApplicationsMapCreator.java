/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.util.FutureUtils;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * @author HyunGil Jeong
 */
public class DefaultApplicationsMapCreator implements ApplicationsMapCreator {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationMapCreator applicationMapCreator;

    private final Executor executor;

    public DefaultApplicationsMapCreator(ApplicationMapCreator applicationMapCreator, Executor executor) {
        this.applicationMapCreator = Objects.requireNonNull(applicationMapCreator, "applicationMapCreator");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public LinkDataDuplexMap createLinkDataDuplexMap(List<Application> applications, LinkSelectContext linkSelectContext) {
        if (CollectionUtils.isEmpty(applications)) {
            return new LinkDataDuplexMap();
        }

        if (applications.size() > 1) {
            return createParallel(applications, linkSelectContext);
        }
        return createSerial(applications, linkSelectContext);
    }

    private LinkDataDuplexMap createSerial(List<Application> applications, LinkSelectContext linkSelectContext) {
        final LinkDataDuplexMap resultMap = new LinkDataDuplexMap();
        for (Application application : applications) {
            LinkDataDuplexMap searchResult = applicationMapCreator.createMap(application, linkSelectContext);
            resultMap.addLinkDataDuplexMap(searchResult);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("depth search. outDepth:{}, inDepth:{}", linkSelectContext.getOutDepth(), linkSelectContext.getInDepth());
        }
        return resultMap;
    }

    private LinkDataDuplexMap createParallel(List<Application> applications, LinkSelectContext linkSelectContext) {
        CompletableFuture<LinkDataDuplexMap>[] futures = getLinkDataMapFutures(applications, linkSelectContext);

        LinkDataDuplexMap[] linkDataDuplexMaps = FutureUtils.allJoin(futures, LinkDataDuplexMap.class);

        LinkDataDuplexMap resultMap = new LinkDataDuplexMap();
        for (LinkDataDuplexMap linkDataDuplexMap : linkDataDuplexMaps) {
            resultMap.addLinkDataDuplexMap(linkDataDuplexMap);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("depth search. inDepth:{}, inDepth:{}", linkSelectContext.getOutDepth(), linkSelectContext.getInDepth());
        }
        return resultMap;
    }


    private CompletableFuture<LinkDataDuplexMap>[] getLinkDataMapFutures(List<Application> targetApplicationList, LinkSelectContext linkSelectContext) {
        @SuppressWarnings("unchecked")
        CompletableFuture<LinkDataDuplexMap>[] linkDataDuplexMapFutures = new CompletableFuture[targetApplicationList.size()];
        for (int i = 0; i < targetApplicationList.size(); i++) {
            final Application targetApplication = targetApplicationList.get(i);

            CompletableFuture<LinkDataDuplexMap> future = CompletableFuture.supplyAsync(new Supplier<>() {
                @Override
                public LinkDataDuplexMap get() {
                    return applicationMapCreator.createMap(targetApplication, linkSelectContext);
                }
            }, executor);
            linkDataDuplexMapFutures[i] = future;
        }
        return linkDataDuplexMapFutures;
    }
}
