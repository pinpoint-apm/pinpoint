package com.navercorp.pinpoint.testapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.service.remote.RemoteService;
import com.navercorp.pinpoint.testapp.util.Description;

@Controller
@RequestMapping("/httpclient4")
public class HttpClient4Controller {
    
    private static final String DEFAULT_GET_GEOCODE_ADDRESS = "Gyeonggi-do, Seongnam-si, Bundang-gu, Jeongja-dong, 178-1";
    private static final String DEFAULT_GET_TWITTER_URL_COUNT_URL = "http://www.naver.com";
    
    @Autowired
    @Qualifier("closeableHttpClientRemoteService")
    RemoteService remoteService;
    
    @RequestMapping("/getGeoCode")
    @Description("")
    @ResponseBody
    public Map<String, Object> getGeoCode(@RequestParam(defaultValue = DEFAULT_GET_GEOCODE_ADDRESS, required = false) String address) throws Exception {
        return remoteService.getGeoCode(address);
    }
    
    @RequestMapping("/getTwitterUrlCount")
    @ResponseBody
    public Map<String, Object> getTwitterUrlCount(@RequestParam(defaultValue = DEFAULT_GET_TWITTER_URL_COUNT_URL, required = false) String url) throws Exception {
        return remoteService.getTwitterUrlCount(url);
    }
    
}
