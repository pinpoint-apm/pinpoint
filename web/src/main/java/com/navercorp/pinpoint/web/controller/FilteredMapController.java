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

import java.util.List;

import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWrap;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author emeroad
 * @author netspider
 */
@Controller
public class FilteredMapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowir    d
	private FilteredMapService filteredMapService;

    @Autowired
    private FilterBuilder filterBuil    er;

	/**
   * filtered server map data query within from ~ to t        eframe
	 *
	 * @param a    plicationName
	 * @para     serviceType    ode
	 * @p    ram from
	 * @para     to
	 * @para     filterT    x
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getFilteredServerMapData", method      RequestMethod.GET)
    @ResponseBody
	publi                                   FilterMapWrap getFilteredServerMa                                  Data(
											@RequestParam("a                                  plicationN                                  me") String applicationName,
											@RequestParam("serviceTypeCode") short serviceTypeC                                  de,
											@RequestParam("from") long from
											@RequestParam("to") long to,
                                                                             @RequestParam("originTo") long originTo,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "hint", required = false) String filterHint,
											@RequestParam(value = "limit", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
        final Filter filter = filterBuilder.build(filterText, filterHint);
        final Range range = new Range(from, to);
        final LimitedScanResult<List<TransactionId>> limitedScanResult = filteredMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit);

        final long lastScanTime = limitedScanResult.getLimitedTime();
        // original range: needed for visual chart data sampling
        final Range originalRange = new Range(from, originTo);
        // needed to figure out already scanned ranged
        final Range scannerRange = new Range(lastScanTime, to);
        logger.debug("originalRange:{} scannerRange:{} ", originalRange, scannerRange);
        ApplicationMap map = filteredMapService.selectApplicationMap(limitedScanResult.getScanData(), originalRange, scannerRange, filter);

        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData         nge scan(limit:{}) range:{} lastFetchedTimestamp:{}", limit, range.prettyT        tring(), DateUtils.long    oDateStr(lastScanTime))
        }

           FilterMapWra     mapWrap    =    new FilterMapWrap(map);
        mapWrap.setLastFetchedTimestamp(lastScanTime);
        return mapWra    ;
	}

	/**
   * filtered server map data query f          r the last "Period" up to now
   *
	 *
	 * @param           pplicationName
	 * @param serviceTypeCode
	 * @pa          am filterText
	 * @param limit          	 * @return
	 */
	@RequestMapping(value = "/getLastFilteredSer          erMapData", method = RequestMethod.GET)
    @ResponseBody
	p          blic FilterMapWrap getLastFilteredServerMapData(
			@RequestParam("applicationName") String applicationName,
			@RequestParam("s       rviceTypeCode") short serviceTypeCo       e,
			@RequestParam("period") long period,
			@RequestParam(value = "filter", required = false) String filterText,
			@RequestParam(value = "hint", required = false) String filte       Hint,
			@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {
        li    it = LimitUtils.checkRange(limit);

		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
    // TODO: since realtime query is enabled for now, calling parameters are fixed as "..., to, to, ..."
    // may need additional @RequestParam("originTo")
		return getFilteredServerMapData(applicationName, serviceTypeCode, from, to, to, filterText, filterHint, limit);
	}


}
