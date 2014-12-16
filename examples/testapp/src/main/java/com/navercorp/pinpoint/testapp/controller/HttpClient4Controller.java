package com.navercorp.pinpoint.testapp.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/httpclient4")
public class HttpClient4Controller {
    
    @RequestMapping("/queryGoogle")
    @ResponseBody
    public String queryGoogle() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createMinimal();
        HttpGet httpGet = new HttpGet(buildQueryUri("www.google.com", "pinpoint"));
        httpClient.execute(httpGet);
        return "queryGoogle";
    }
    
    @RequestMapping("/queryNaver")
    @ResponseBody
    public String queryNaver() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createMinimal();
        HttpGet httpGet = new HttpGet(buildQueryUri("www.naver.com", "pinpoint"));
        httpClient.execute(httpGet);
        return "queryNaver";
    }
    
    private URI buildQueryUri(String host, String queryParam) throws URISyntaxException {
        return new URIBuilder()
            .setScheme("http")
            .setHost(host)
            .setPath("/search")
            .setParameter("q", queryParam)
            .build();
    }
}
