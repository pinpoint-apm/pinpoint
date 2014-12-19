package com.navercorp.pinpoint.testapp.controller;

import java.util.Map;

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

    RemoteService remoteOperations = new HttpRemoteService();

    @RequestMapping("/getGeoCode")
    @Description("")
    @ResponseBody
    public Map<String, Object> getGeoCode(@RequestParam(defaultValue = DEFAULT_GET_GEOCODE_ADDRESS, required = false) String address) throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("address", address);
        params.add("sensor", "false");

        return remoteOperations.get(GOOGLE_GEOCODE_URL, params, Map.class);
    }

    @RequestMapping("/getTwitterUrlCount")
    @ResponseBody
    public Map<String, Object> getTwitterUrlCount(@RequestParam(defaultValue = DEFAULT_GET_TWITTER_URL_COUNT_URL, required = false) String url) throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("url", url);
        
        return remoteOperations.get(TWITTER_URL_COUNT_URL, params, Map.class);
    }

    @RequestMapping("/getTwitterUrlCountByPost")
    @ResponseBody
    public Map<String, Object> getTwitterUrlCountByPost(@RequestParam(defaultValue = DEFAULT_GET_TWITTER_URL_COUNT_URL, required = false) String url) throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("url", url);
        
        return remoteOperations.post(TWITTER_URL_COUNT_URL, params, Map.class);
    }

}
