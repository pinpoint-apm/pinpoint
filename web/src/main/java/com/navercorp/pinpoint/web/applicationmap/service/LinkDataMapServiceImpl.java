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

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.dao.MapInLinkDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Service
public class LinkDataMapServiceImpl implements LinkDataMapService {

    private final MapOutLinkDao mapOutLinkDao;

    private final MapInLinkDao mapInLinkDao;

    public LinkDataMapServiceImpl(MapOutLinkDao mapOutLinkDao, MapInLinkDao mapInLinkDao) {
        this.mapOutLinkDao = Objects.requireNonNull(mapOutLinkDao, "mapOutLinkDao");
        this.mapInLinkDao = Objects.requireNonNull(mapInLinkDao, "mapInLinkDao");
    }

    @Override
    public LinkDataMap selectOutLinkDataMap(Application outApplication, TimeWindow timeWindow, boolean timeAggregated) {
        return mapOutLinkDao.selectOutLink(outApplication, timeWindow, timeAggregated);
    }

    @Override
    public LinkDataMap selectInLinkDataMap(Application inApplication, TimeWindow timeWindow, boolean timeAggregated) {
        return mapInLinkDao.selectInLink(inApplication, timeWindow, timeAggregated);
    }
}
