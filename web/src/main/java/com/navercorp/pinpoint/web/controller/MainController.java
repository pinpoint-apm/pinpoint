/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.CacheService;
import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.util.etag.ETag;
import com.navercorp.pinpoint.web.util.etag.ETagUtils;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.ApplicationGroup;
import com.navercorp.pinpoint.web.view.ServerTime;
import com.navercorp.pinpoint.web.view.TagApplications;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 */
@RestController
@Validated
public class MainController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CommonService commonService;

    private final CacheService cacheService;

    // Reserve key
    private static final String KEY = CacheService.DEFAULT_KEY;

    public MainController(
            CommonService commonService,
            CacheService cacheService
    ) {
        this.commonService = Objects.requireNonNull(commonService, "commonService");
        this.cacheService = Objects.requireNonNull(cacheService, "cacheService");
    }

    @GetMapping(value = "/api/applications")
    public ResponseEntity<ApplicationGroup> getApplicationGroup(
            @RequestHeader(value = "If-None-Match", required = false) @NullOrNotBlank String eTagHeader,
            @RequestParam(value = "clearCache", required = false) @NullOrNotBlank String clearCache
    ) {
        final ETag eTag = ETagUtils.parseETag(eTagHeader);
        if (needClearCache(eTag, clearCache)) {
            cacheService.remove(KEY);
        }

        if (eTag != null) {
            logger.debug("eTag: {}", eTag);

            final TagApplications cachedApplications = cacheService.get(KEY);
            if (cachedApplications != null) {
                if (eTag.tag().equals(cachedApplications.getTag())) {
                    logger.debug("applicationList {} cache hit", KEY);
                    return notModified();
                } else {
                    logger.debug("applicationList {} cache hit, but missed eTag {} = {}",
                            KEY, clearCache, cachedApplications.getTag());
                }
            } else {
                // ETag changed by another node.
                logger.debug("applicationList {} cache missed", KEY);
            }
        }

        final List<Application> applicationList = commonService.selectAllApplicationNames();
        logger.debug("/applications size: {}", applicationList.size());
        logger.trace("/applications {}", applicationList);


        // Update atomicity between multiple nodes is not guaranteed
        final TagApplications tagApplications = wrapApplicationList(applicationList);

        cacheService.put(KEY, tagApplications);

        final ETag newETag = new ETag(true, tagApplications.getTag());
        logger.debug("eTag cache {} -> {}", eTag, newETag);

        // force
        // !etag.isWeak()
        // if (cachedApplications.getApplicationList().equals(applicationList))
        //
        // weak
        return ResponseEntity.ok()
                .eTag(newETag.toString())
                .body(new ApplicationGroup(applicationList));
    }

    private static TagApplications wrapApplicationList(List<Application> applicationList) {
        String tag = newTag(applicationList);
        return new TagApplications(tag, applicationList);
    }


    private static String newTag(List<Application> applicationList) {
        // Precondition : If the application list of hbase is the same,
        // ETag value of multiple web servers is also the same.
        // need MD5 hash (128 bit)
        return String.valueOf(applicationList.hashCode());
    }

    private static ResponseEntity<ApplicationGroup> notModified() {
        return ResponseEntity
                .status(HttpStatus.NOT_MODIFIED)
                .build();
    }

    private static boolean needClearCache(ETag eTag, String clearCache) {
        return eTag == null || clearCache != null;
    }

    @GetMapping(value = {"/api/serverTime", "/api-public/serverTime"})
    public ServerTime getServerTime() {
        return new ServerTime();
    }

}
