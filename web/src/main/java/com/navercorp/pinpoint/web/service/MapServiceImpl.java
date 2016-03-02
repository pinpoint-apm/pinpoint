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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.rawdata.*;
import com.navercorp.pinpoint.web.dao.*;
import com.navercorp.pinpoint.web.vo.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 */
@Service
public class MapServiceImpl implements MapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private MapResponseDao mapResponseDao;

    @Autowired
    private MapStatisticsCalleeDao mapStatisticsCalleeDao;

    @Autowired
    private MapStatisticsCallerDao mapStatisticsCallerDao;

    @Autowired
    private HostApplicationMapDao hostApplicationMapDao;

    @Autowired
    private ApplicationFactory applicationFactory;

    /**
     * Used in the main UI - draws the server map by querying the timeslot by time.
     */
    @Override
    public ApplicationMap selectApplicationMap(Application sourceApplication, Range range, SearchOption searchOption) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        logger.debug("SelectApplicationMap");

        StopWatch watch = new StopWatch("ApplicationMap");
        watch.start("ApplicationMap Hbase Io Fetch(Caller,Callee) Time");

        LinkSelector linkSelector = new BFSLinkSelector(this.mapStatisticsCallerDao, this.mapStatisticsCalleeDao, hostApplicationMapDao);
        LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplication, range, searchOption);
        watch.stop();

        watch.start("ApplicationMap MapBuilding(Response) Time");
        ApplicationMapBuilder builder = new ApplicationMapBuilder(range);
        ApplicationMap map = builder.build(linkDataDuplexMap, agentInfoService, this.mapResponseDao);
        if (map.getNodes().isEmpty()) {
            map = builder.build(sourceApplication, agentInfoService);
        }
        watch.stop();
        if (logger.isInfoEnabled()) {
            logger.info("ApplicationMap BuildTime: {}", watch.prettyPrint());
        }

        return map;
    }


}
