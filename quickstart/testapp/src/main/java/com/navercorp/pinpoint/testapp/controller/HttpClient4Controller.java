package com.navercorp.pinpoint.testapp.controller;

import com.navercorp.pinpoint.testapp.service.remote.RemoteService;
import com.navercorp.pinpoint.testapp.util.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author koo.taejin
 */
@Controller
@RequestMapping("/httpclient4")
public class HttpClient4Controller {

    private static final String GOOGLE_GEOCODE_URL = "http://maps.googleapis.com/maps/api/geocode/json";
    private static final String EPL_LEAGUE_TABLE_URL = "http://api.football-data.org/v1/soccerseasons/398/leagueTable";

    private static final String DEFAULT_GET_GEOCODE_ADDRESS = "Gyeonggi-do, Seongnam-si, Bundang-gu, Jeongja-dong, 178-1";

    @Autowired
    @Qualifier("httpRemoteService")
    RemoteService remoteService;

    @RequestMapping("/getGeoCode")
    @ResponseBody
    @Description("HTTP GET to " + GOOGLE_GEOCODE_URL)
    public Map<String, Object> getGeoCode(@RequestParam(defaultValue = DEFAULT_GET_GEOCODE_ADDRESS, required = false) String address) throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("address", address);
        params.add("sensor", "false");

        return remoteService.get(GOOGLE_GEOCODE_URL, params, Map.class);
    }

    @RequestMapping("/getEPLLeagueTable")
    @ResponseBody
    @Description("HTTP GET to " + EPL_LEAGUE_TABLE_URL)
    public Map<String, Object> getEplLeagueTable() throws Exception {
        return remoteService.get(EPL_LEAGUE_TABLE_URL, Map.class);
    }

}
