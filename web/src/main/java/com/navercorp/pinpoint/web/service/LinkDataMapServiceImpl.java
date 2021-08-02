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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.MapStatisticsCalleeCompactDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerCompactDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Service
public class LinkDataMapServiceImpl implements LinkDataMapService {
    private final MapStatisticsCallerCompactDao mapStatisticsCallerCompactDao;
    private final MapStatisticsCalleeCompactDao mapStatisticsCalleeCompactDao;

    public LinkDataMapServiceImpl(MapStatisticsCallerCompactDao mapStatisticsCallerCompactDao, MapStatisticsCalleeCompactDao mapStatisticsCalleeCompactDao) {
        this.mapStatisticsCallerCompactDao = Objects.requireNonNull(mapStatisticsCallerCompactDao, "mapStatisticsCallerCompactDao");
        this.mapStatisticsCalleeCompactDao = Objects.requireNonNull(mapStatisticsCalleeCompactDao, "mapStatisticsCalleeCompactDao");
    }

    @Override
    public LinkDataMap selectCallerLinkDataMap(Application application, Range range) {
        return this.mapStatisticsCallerCompactDao.selectCaller(application, range);
    }

    @Override
    public LinkDataMap selectCalleeLinkDataMap(Application application, Range range) {
        return mapStatisticsCalleeCompactDao.selectCallee(application, range);
    }
}
