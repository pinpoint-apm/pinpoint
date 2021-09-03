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
import com.navercorp.pinpoint.web.view.ApplicationGroup;
import com.navercorp.pinpoint.web.view.ServerTime;
import com.navercorp.pinpoint.web.view.TagApplications;
import com.navercorp.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 */
@RestController
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonService commonService;

    private final CacheService cacheService;

    // Reserve key
    private final String key = CacheService.DEFAULT_KEY;

    public MainController(CommonService commonService,
                          CacheService cacheService) {
        this.commonService = Objects.requireNonNull(commonService, "commonService");
        this.cacheService = Objects.requireNonNull(cacheService, "cacheService");
    }

    @GetMapping(value = "/applications")
    public ResponseEntity<ApplicationGroup> getApplicationGroup(
            @RequestHeader(value = "If-None-Match", required = false) String eTagHeader,
            @RequestParam(value = "clearCache", required = false) String clearCache) {

        final ETag eTag = ETagUtils.parseETag(eTagHeader);
        if (needClearCache(eTag, clearCache)) {
            cacheService.remove(key);
        }

        TagApplications cachedApplications;
        if (eTag != null) {
            logger.debug("eTag:{} ", eTag);

            cachedApplications = cacheService.get(key);
            if (cachedApplications != null) {
                if (eTag.getTag().equals(cachedApplications.getTag())) {
                    logger.debug("applicationList {} cache hit", key);
                    return notModified();
                } else {
                    logger.debug("applicationList {} cache hit, ETag miss {}={}", key, clearCache, cachedApplications.getTag());
                }
            } else {
                // ETag changed by another node.
                logger.debug("applicationList {} cache miss", key);
            }
        }

        List<Application> applicationList = commonService.selectAllApplicationNames();
        logger.debug("/applications size:{}", applicationList.size());
        logger.trace("/applications {}", applicationList);


        // Update atomicity between multiple nodes is not guaranteed
        TagApplications tagApplications = wrapApplicationList(applicationList);

        cacheService.put(key, tagApplications);

        ETag newETag = new ETag(true, tagApplications.getTag());
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

    private TagApplications wrapApplicationList(List<Application> applicationList) {
        String tag = newTag(applicationList);
        return new TagApplications(tag, applicationList);
    }


    private String newTag(List<Application> applicationList) {
        // Precondition : If the application list of hbase is the same,
        // ETag value of multiple web servers is also the same.
        // need MD5 hash (128 bit)
        return String.valueOf(applicationList.hashCode());
    }

    private ResponseEntity<ApplicationGroup> notModified() {
        return ResponseEntity
                .status(HttpStatus.NOT_MODIFIED)
                .build();
    }

    private boolean needClearCache(ETag eTag, String clearCache) {
        return eTag == null || clearCache != null;
    }

    @GetMapping(value = "/serverTime")
    public ServerTime getServerTime() {
        return new ServerTime();
    }

}
