package com.nhn.pinpoint.web.controller;

import java.util.List;

import com.nhn.pinpoint.web.applicationmap.FilterMapWrap;
import com.nhn.pinpoint.web.service.FilteredMapService;
import com.nhn.pinpoint.web.util.LimitUtils;
import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.util.DateUtils;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.filter.FilterBuilder;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.TransactionId;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author emeroad
 * @author netspider
 */
@Controller
public class FilteredMapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FilteredMapService filteredMapService;

    @Autowired
    private FilterBuilder filterBuilder;

	/**
	 * 필터가 적용된 서버맵의 FROM ~ TO기간의 데이터 조회
	 * 
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @param filterText
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getFilteredServerMapData", method = RequestMethod.GET)
    @ResponseBody
	public FilterMapWrap getFilteredServerMapData(
											@RequestParam("application") String applicationName,
											@RequestParam("serviceType") short serviceType,
											@RequestParam("from") long from,
											@RequestParam("to") long to,
                                            @RequestParam("originTo") long originTo,
											@RequestParam(value = "filter", required = false) String filterText,
											@RequestParam(value = "limit", required = false, defaultValue = "10000") int limit) {
        limit = LimitUtils.checkRange(limit);
        final Filter filter = filterBuilder.build(filterText);
        // scan을 해야 될 토탈 범위
        final Range range = new Range(from, to);
        final LimitedScanResult<List<TransactionId>> limitedScanResult = filteredMapService.selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit);

        final long lastScanTime = limitedScanResult.getLimitedTime();
        // 원본 범위, 시계열 차트의 sampling을 하려면 필요함.
        final Range originalRange = new Range(from, originTo);
        // 정확히 스캔된 범위가 어디까지 인지 알기 위해서 필요함.
        final Range scannerRange = new Range(lastScanTime, to);
        logger.debug("originalRange:{} scannerRange:{} ", originalRange, scannerRange);
        ApplicationMap map = filteredMapService.selectApplicationMap(limitedScanResult.getScanData(), originalRange, scannerRange, filter);
		
        if (logger.isDebugEnabled()) {
            logger.debug("getFilteredServerMapData range scan(limit:{}) range:{} lastFetchedTimestamp:{}", limit, range.prettyToString(), DateUtils.longToDateStr(lastScanTime));
        }

        FilterMapWrap mapWrap = new FilterMapWrap(map);
        mapWrap.setLastFetchedTimestamp(lastScanTime);
        return mapWrap;
	}
	
	/**
	 * 필터가 적용된 서버맵의 Period before 부터 현재시간까지의 데이터 조회.
	 * 
	 * @param applicationName
	 * @param serviceType
	 * @param filterText
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/getLastFilteredServerMapData", method = RequestMethod.GET)
    @ResponseBody
	public FilterMapWrap getLastFilteredServerMapData(
			@RequestParam("application") String applicationName,
			@RequestParam("serviceType") short serviceType,
			@RequestParam("period") long period,
			@RequestParam(value = "filter", required = false) String filterText,
			@RequestParam(value = "limit", required = false, defaultValue = "1000000") int limit) {
        limit = LimitUtils.checkRange(limit);

		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
        // TODO 실시간 조회가 현재 disable이므로 to to로 수정하였음. 이것도 추가적으로 @RequestParam("originTo")가 필요할수 있음.
		return getFilteredServerMapData(applicationName, serviceType, from, to, to, filterText, limit);
	}


}