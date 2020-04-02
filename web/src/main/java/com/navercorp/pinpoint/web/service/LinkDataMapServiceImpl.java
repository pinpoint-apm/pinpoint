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
import com.navercorp.pinpoint.web.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Service
public class LinkDataMapServiceImpl implements LinkDataMapService {

    private final MapStatisticsCallerDao mapStatisticsCallerDao;

    private final MapStatisticsCalleeDao mapStatisticsCalleeDao;

    public LinkDataMapServiceImpl(MapStatisticsCallerDao mapStatisticsCallerDao, MapStatisticsCalleeDao mapStatisticsCalleeDao) {
        this.mapStatisticsCallerDao = Objects.requireNonNull(mapStatisticsCallerDao, "mapStatisticsCallerDao");
        this.mapStatisticsCalleeDao = Objects.requireNonNull(mapStatisticsCalleeDao, "mapStatisticsCalleeDao");
    }

    @Override
    public LinkDataMap selectCallerLinkDataMap(Application application, Range range) {
        return mapStatisticsCallerDao.selectCaller(application, range);
    }

    @Override
    public LinkDataMap selectCalleeLinkDataMap(Application application, Range range) {
        return mapStatisticsCalleeDao.selectCallee(application, range);
    }
}
