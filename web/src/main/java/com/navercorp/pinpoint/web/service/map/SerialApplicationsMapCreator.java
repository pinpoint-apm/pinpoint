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

package com.navercorp.pinpoint.web.service.map;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SerialApplicationsMapCreator implements ApplicationsMapCreator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationMapCreator applicationMapCreator;

    public SerialApplicationsMapCreator(ApplicationMapCreator applicationMapCreator) {
        if (applicationMapCreator == null) {
            throw new NullPointerException("applicationMapCreator must not be null");
        }
        this.applicationMapCreator = applicationMapCreator;
    }

    @Override
    public LinkDataDuplexMap createLinkDataDuplexMap(List<Application> applications, LinkSelectContext linkSelectContext) {
        final LinkDataDuplexMap resultMap = new LinkDataDuplexMap();
        for (Application application : applications) {
            LinkDataDuplexMap searchResult = applicationMapCreator.createMap(application, linkSelectContext);
            resultMap.addLinkDataDuplexMap(searchResult);
        }
        logger.debug("depth search end. callerDepth : {}, calleeDepth : {}", linkSelectContext.getCallerDepth(), linkSelectContext.getCalleeDepth());
        return resultMap;
    }
}
