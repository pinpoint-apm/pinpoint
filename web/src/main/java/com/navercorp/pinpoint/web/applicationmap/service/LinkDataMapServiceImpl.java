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

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.InboundDao;
import com.navercorp.pinpoint.web.applicationmap.dao.OutboundDao;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.calltree.span.Link;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Service
public class LinkDataMapServiceImpl implements LinkDataMapService {

    @Value("${pinpoint.modules.web.application-based-server-map.enabled:true}")
    private boolean isApplicationMapEnabled;
    private final MapOutLinkDao mapOutLinkDao;

    private final MapInLinkDao mapInLinkDao;

    private final InboundDao inboundDao;
    private final OutboundDao outboundDao;

    public LinkDataMapServiceImpl(MapOutLinkDao mapOutLinkDao, MapInLinkDao mapInLinkDao,
            OutboundDao outboundDao, InboundDao inboundDao) {
        this.mapOutLinkDao = Objects.requireNonNull(mapOutLinkDao, "mapOutLinkDao");
        this.mapInLinkDao = Objects.requireNonNull(mapInLinkDao, "mapInLinkDao");
        this.outboundDao = Objects.requireNonNull(outboundDao, "serviceGroupOutboundDao");
        this.inboundDao = Objects.requireNonNull(inboundDao, "serviceGroupInboundDao");
    }

    @Override
    public LinkDataMap selectOutLinkDataMap(Application outApplication, Range range, boolean timeAggregated) {
        LinkDataMap linkDataMap = mapOutLinkDao.selectOutLink(outApplication, range, timeAggregated);
        LinkDataMap linkDataMap1 = outboundDao.selectOutboud(outApplication, range, timeAggregated);
        if (isApplicationMapEnabled) {
            return linkDataMap1;
        }
        return linkDataMap;
    }

    @Override
    public LinkDataMap selectInLinkDataMap(Application inApplication, Range range, boolean timeAggregated) {
        LinkDataMap linkDataMap = mapInLinkDao.selectInLink(inApplication, range, timeAggregated);
        LinkDataMap linkDataMap1 = inboundDao.selectInbound(inApplication, range, timeAggregated);
        if (isApplicationMapEnabled) {
            return linkDataMap1;
        }
        return linkDataMap;
    }
}