package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.configuration.DemoURLHolder;
import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.connector.ningasync.NingAsyncHttpClient;
import com.nhn.pinpoint.testweb.service.CacheService;
import com.nhn.pinpoint.testweb.service.CubridService;
import com.nhn.pinpoint.testweb.service.MemberService;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author netspider
 */
@Controller
public class DemoController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DemoURLHolder urls;

    private final Random random = new Random();

    @Autowired
    private CacheService cacheService;

    @Autowired
    @Qualifier("memberService")
    private MemberService mysqlService;

    @Autowired
    private CubridService cubridService;

    @Autowired
    private NingAsyncHttpClient ningAsyncHttpClient;

    @Autowired
    private ApacheHttpClient4 apacheHttpClient;

    public DemoController() {
        urls = DemoURLHolder.getHolder();
    }

    @RequestMapping(value = "/netspider")
    @ResponseBody
    public String demo1() {
        accessNaverBlog();
        accessNaverCafe();
        randomSlowMethod();
        callRemote(urls.getBackendApiURL());
        return "OK";
    }

    @RequestMapping(value = "/emeroad")
    @ResponseBody
    public String demo2() {
        randomSlowMethod();
        callRemote(urls.getBackendWebURL());
        return "OK";
    }

    @RequestMapping(value = "/harebox")
    @ResponseBody
    public String demo3() {
        cacheService.memcached();
        accessNaver();
        return "OK";
    }

    @RequestMapping(value = "/denny")
    @ResponseBody
    public String demo4() {
        mysqlService.list();
        randomSlowMethod();
        return "OK";
    }

    @RequestMapping(value = "/backendweb")
    @ResponseBody
    public String backendweb() {
        cacheService.arcus();
        mysqlService.list();
        if (random.nextBoolean()) {
            callRemote(urls.getBackendApiURL());
        }
        return "OK";
    }

    @RequestMapping(value = "/backendapi")
    @ResponseBody
    public String backendapi() {
        mysqlService.list();
        cubrid();
        return "OK";
    }

    private void callRemote(String url) {
        apacheHttpClient.execute(url, new HashMap<String, Object>());
    }

    private void cubrid() {
        switch (new Random().nextInt(3)) {
            case 1:
                cubridService.createErrorStatement();
            case 2:
                cubridService.createStatement();
            case 3:
                cubridService.selectOne();
        }
    }

    private void accessNaver() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", "naver");
        params.put("ie", "utf8");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header1", "header1");
        headers.put("header2", "header2");

        List<Cookie> cookies = new ArrayList<Cookie>();
        cookies.add(new Cookie("cookieName1", "cookieValue1", "cookieRawValue1", "", "/", 10, 10, false, false));
        cookies.add(new Cookie("cookieName2", "cookieValue2", "cookieRawValue2", "", "/", 10, 10, false, false));

        ningAsyncHttpClient.requestGet("http://search.naver.com/search.naver?where=nexearch", params, headers, cookies);
    }

    private void accessNaverBlog() {
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://section.blog.naver.com/", new HashMap<String, Object>());
    }

    private void accessNaverCafe() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection) new URL("http://section.cafe.naver.com/").openConnection();
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.warn("reader.close() error {}", e.getMessage(), e);
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void randomSlowMethod() {
        try {
            Thread.sleep(((new Random().nextInt(90)) + 10) * 10L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
