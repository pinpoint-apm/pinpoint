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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.service.CacheService;
import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.view.ApplicationGroup;
import com.navercorp.pinpoint.web.view.ServerTime;
import com.navercorp.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author emeroad
 * @author netspider
 */
@RestController
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonService commonService;

    private final CacheService cacheService;

    public MainController(CommonService commonService, CacheService cacheService) {
        this.commonService = Objects.requireNonNull(commonService, "commonService");
        this.cacheService = Objects.requireNonNull(cacheService, "cacheService");
    }

    @GetMapping(value = "/applications")
    public ResponseEntity<ApplicationGroup> getApplicationGroup(
            @RequestHeader(value = "If-None-Match", required = false) String eTag,
            @RequestParam(value="clearCache", required = false) String clearCache
    ) {

        final boolean needClearCache = StringUtils.isEmpty(eTag) || clearCache != null;
        if (needClearCache) {
            cacheService.clearApplicationListCache();
        }

        List<Application> applicationList = commonService.selectAllApplicationNames();
        logger.debug("/applications size:{}", applicationList.size());
        logger.trace("/applications {}", applicationList);

        final String newETag = cacheService.getApplicationListETag();
        final boolean noUpdate = eTag != null && eTag.replace("\"", "").contentEquals(newETag);
        if (noUpdate) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).contentLength(0L).build();
        } else {
            return ResponseEntity.ok().eTag(newETag).body(new ApplicationGroup(applicationList));
        }
    }

    @GetMapping(value = "/serverTime")
    public ServerTime getServerTime() {
        return new ServerTime();
    }

}
