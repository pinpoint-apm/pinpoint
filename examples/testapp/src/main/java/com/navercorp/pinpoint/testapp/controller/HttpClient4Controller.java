package com.navercorp.pinpoint.testapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.service.remote.HttpRemoteService;
import com.navercorp.pinpoint.testapp.service.remote.RemoteService;
import com.navercorp.pinpoint.testapp.util.Description;

/**
 * @author koo.taejin
 */
@Controller
@RequestMapping("/httpclient4")
public class HttpClient4Controller {

    private static final String GOOGLE_GEOCODE_URL = "http://maps.googleapis.com/maps/api/geocode/json";
    private static final String TWITTER_URL_COUNT_URL = "http://urls.api.twitter.com/1/urls/count.json";

    private static final String DEFAULT_GET_GEOCODE_ADDRESS = "Gyeonggi-do, Seongnam-si, Bundang-gu, Jeongja-dong, 178-1";
    private static final String DEFAULT_GET_TWITTER_URL_COUNT_URL = "http://www.naver.com";

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

    @RequestMapping("/getTwitterUrlCount")
    @ResponseBody
    @Description("HTTP GET to " + TWITTER_URL_COUNT_URL)
    public Map<String, Object> getTwitterUrlCount(@RequestParam(defaultValue = DEFAULT_GET_TWITTER_URL_COUNT_URL, required = false) String url) throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("url", url);
        
        return remoteService.get(TWITTER_URL_COUNT_URL, params, Map.class);
    }

    @RequestMapping("/getTwitterUrlCountByPost")
    @ResponseBody
    @Description("HTTP POST to " + TWITTER_URL_COUNT_URL)
    public Map<String, Object> getTwitterUrlCountByPost(@RequestParam(defaultValue = DEFAULT_GET_TWITTER_URL_COUNT_URL, required = false) String url) throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("url", url);
        
        return remoteService.post(TWITTER_URL_COUNT_URL, params, Map.class);
    }

}
