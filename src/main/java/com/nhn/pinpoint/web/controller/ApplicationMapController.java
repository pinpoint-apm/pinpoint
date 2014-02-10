package com.nhn.pinpoint.web.controller;

import javax.servlet.http.HttpServletResponse;

import com.nhn.pinpoint.web.util.Limiter;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.service.ApplicationMapService;
import com.nhn.pinpoint.web.util.TimeUtils;
import com.nhn.pinpoint.web.vo.LinkStatistics;

/**
 * 
 * @author netspider
 */
@Controller
public class ApplicationMapController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
	private ApplicationMapService applicationMapService;

    @Autowired
    private Limiter dateLimit;

	/**
	 * FROM ~ TO기간의 서버 맵 데이터 조회
	 * 
	 * @param model
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @return
	 */
	@RequestMapping(value = "/getServerMapData", method = RequestMethod.GET)
	public String getServerMapData(Model model,
									@RequestParam("application") String applicationName,
									@RequestParam("serviceType") short serviceType, 
									@RequestParam("from") long from,
									@RequestParam("to") long to) {
		this.dateLimit.limit(from, to);

        Application application = new Application(applicationName, serviceType);
        ApplicationMap map = applicationMapService.selectApplicationMap(application, from, to);

		model.addAttribute("nodes", map.getNodes());
		model.addAttribute("links", map.getLinks());

		return "applicationmap";
	}

	/**
	 * Period before 부터 현재시간까지의 서버맵 조회.
	 * 
	 * @param model
	 * @param applicationName
	 * @param serviceType
	 * @param period
	 * @return
	 */
	@RequestMapping(value = "/getLastServerMapData", method = RequestMethod.GET)
	public String getLastServerMapData(Model model,
										@RequestParam("application") String applicationName,
										@RequestParam("serviceType") short serviceType,
										@RequestParam("period") long period) {
		
		long to = TimeUtils.getDelayLastTime();
		long from = to - period;
		return getServerMapData(model, applicationName, serviceType, from, to);
	}

	/**
	 * 필터가 사용되지 않은 서버맵의 연결선을 통과하는 요청의 통계정보 조회
	 * 
	 * @param model
	 * @param from
	 * @param to
	 * @param srcApplicationName
	 * @param srcServiceType
	 * @param destApplicationName
	 * @param destServiceType
	 * @return
	 */
	@RequestMapping(value = "/linkStatistics", method = RequestMethod.GET)
	public String getLinkStatistics(Model model,
									@RequestParam("from") long from,
									@RequestParam("to") long to,
									@RequestParam("srcApplicationName") String srcApplicationName,
									@RequestParam("srcServiceType") short srcServiceType,
									@RequestParam("destApplicationName") String destApplicationName,
									@RequestParam("destServiceType") short destServiceType,
									@RequestParam(value="v", required=false, defaultValue="1") int v) {

        final Application sourceApplication = new Application(srcApplicationName, srcServiceType);
        final Application destinationApplication = new Application(srcApplicationName, srcServiceType);

		LinkStatistics linkStatistics = applicationMapService.linkStatistics(from, to, sourceApplication, destinationApplication);

		model.addAttribute("from", from);
		model.addAttribute("to", to);

		model.addAttribute("srcApplication", sourceApplication);

        model.addAttribute("destApplication", destinationApplication);

		model.addAttribute("linkStatistics", linkStatistics);
//		model.addAttribute("histogramSummary", linkStatistics.getHistogramSummary().entrySet().iterator());
		model.addAttribute("timeseriesSlotIndex", linkStatistics.getTimeseriesSlotIndex());
		model.addAttribute("timeseriesValue", linkStatistics.getTimeseriesValue());
		
		model.addAttribute("resultFrom", from);
		model.addAttribute("resultTo", to);
		
		if (v == 2) {
			return "linkStatistics2";
		} else {
			return "linkStatistics";
		}
	}
}