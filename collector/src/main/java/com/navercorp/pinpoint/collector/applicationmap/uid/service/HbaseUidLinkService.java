/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.applicationmap.uid.service;

import com.navercorp.pinpoint.collector.applicationmap.SelfUidVertex;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapInLinkUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapOutLinkUidDao;
import com.navercorp.pinpoint.collector.applicationmap.uid.dao.MapSelfUidDao;
import com.navercorp.pinpoint.collector.service.UidLinkService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HbaseUidLinkService implements UidLinkService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapSelfUidDao mapSelfUidDao;
    private final MapOutLinkUidDao mapOutLinkUidDao;
    private final MapInLinkUidDao mapInLinkUidDao;

    public HbaseUidLinkService(MapSelfUidDao mapSelfUidDao,
                               MapOutLinkUidDao mapOutLinkUidDao,
                               MapInLinkUidDao mapInLinkUidDao) {
        this.mapSelfUidDao = Objects.requireNonNull(mapSelfUidDao, "mapSelfUidDao");
        this.mapOutLinkUidDao = Objects.requireNonNull(mapOutLinkUidDao, "outLinkUidDao");
        this.mapInLinkUidDao = Objects.requireNonNull(mapInLinkUidDao, "mapInLinkUidDao");
    }

    @Override
    public void updateResponseTime(
            long requestTime,
            SelfUidVertex selfVertex,
            int elapsed, boolean isError
    ) {
        logger.debug("updateResponseTime {}", selfVertex);
        mapSelfUidDao.self(requestTime, selfVertex, elapsed, isError);
    }

    @Override
    public void updateOutLink(
            long requestTime,
            SelfUidVertex selfVertex,

            String outLinkApplicationName,
            ServiceType outLinkServiceType,
            String outHost,
            int elapsed, boolean isError
    ) {
        logger.debug("insertOutLink {}", selfVertex);
        mapOutLinkUidDao.insertOutLink(requestTime, selfVertex, outLinkApplicationName, outLinkServiceType, outHost, elapsed, isError);
    }


    @Override
    public void updateInLink(
            long requestTime,
            String inLinkApplicationName,
            ServiceType inLinkServiceType,

            SelfUidVertex selfVertex,
            String selfSubLink,
            int elapsed, boolean isError
    ) {
        logger.debug("inLink {}", selfVertex);
        mapInLinkUidDao.insertInLink(requestTime, inLinkApplicationName, inLinkServiceType, selfVertex, selfSubLink, elapsed, isError);
    }
}
