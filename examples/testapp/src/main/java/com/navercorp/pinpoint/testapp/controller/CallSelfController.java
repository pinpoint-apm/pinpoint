package com.navercorp.pinpoint.testapp.controller;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.service.remote.RemoteService;
import com.navercorp.pinpoint.testapp.util.Description;

@Controller
@RequestMapping("/callSelf")
public class CallSelfController {
    
    private static final String GET_CURRENT_TIMESTAMP_PATH = "/getCurrentTimestamp";
    private static final String GET_GEO_CODE_PATH = "/httpclient4/getGeoCode";
    private static final String GET_TWITTER_URL_COUNT_PATH = "/httpclient4/getTwitterUrlCount";
    
    private static final String DEFAULT_LOCAL_IP = "127.0.0.1";
    private static final String LOCAL_IP = getLocalHostIp();
    
    private static String getLocalHostIp() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
        }
        return DEFAULT_LOCAL_IP;
    }
    
    @Autowired
    @Qualifier("httpRemoteService")
    RemoteService remoteService;

    @RequestMapping("/getCurrentTimestamp")
    @ResponseBody
    @Description("Calls self for " + GET_CURRENT_TIMESTAMP_PATH + " over HTTP.")
    public Map<String, Object> getCurrentTimeStamp(HttpServletRequest request) throws Exception {
        String url = createTargetUrl(request, GET_CURRENT_TIMESTAMP_PATH);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = remoteService.get(url, Map.class);
        return response;
    }
    
    @RequestMapping("/httpclient4/getGeoCode")
    @ResponseBody
    @Description("Calls self for " + GET_GEO_CODE_PATH + " over HTTP.")
    public Map<String, Object> httpClient4GetGeoCode(HttpServletRequest request) throws Exception {
        String url = createTargetUrl(request, GET_GEO_CODE_PATH);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = remoteService.get(url, Map.class);
        return response;
    }
    
    @RequestMapping("/httpclient4/getTwitterUrlCount")
    @ResponseBody
    @Description("Calls self for " + GET_TWITTER_URL_COUNT_PATH + " over HTTP.")
    public Map<String, Object> httpClient4GetTwitterUrlCount(HttpServletRequest request) throws Exception {
        String url = createTargetUrl(request, GET_TWITTER_URL_COUNT_PATH);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = remoteService.get(url, Map.class);
        return response;
    }
    
    private static final String createTargetUrl(final HttpServletRequest request, final String path) throws URISyntaxException {
        return new URIBuilder()
                .setScheme("http")
                .setHost(LOCAL_IP)
                .setPort(request.getLocalPort())
                .setPath(path + ".pinpoint")
                .build()
                .toString();
    }
    
}
