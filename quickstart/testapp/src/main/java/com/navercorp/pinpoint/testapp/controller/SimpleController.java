package com.navercorp.pinpoint.testapp.controller;

import com.navercorp.pinpoint.testapp.util.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author koo.taejin
 */
@RestController
public class SimpleController {

    @RequestMapping("/getCurrentTimestamp")
    @Description("Returns the server's current timestamp.")
    public Map<String, Object> getCurrentTimestamp() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("getCurrentTimestamp", System.currentTimeMillis());

        return map;
    }

    @RequestMapping("/testUserInputRequestAttribute")
    public Map<String, Object> testUserInputAttribute(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "test user input attribute");
        request.setAttribute("pinpoint.metric.uri-template", "/userInput");
        return map;
    }

    @RequestMapping("/sleep3")
    @Description("Call that takes 3 seconds to complete.")
    public Map<String, Object> sleep3() throws InterruptedException {
        Thread.sleep(3000);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }

    @RequestMapping("/sleep5")
    @Description("Call that takes 5 seconds to complete")
    public Map<String, Object> sleep5() throws InterruptedException {
        Thread.sleep(5000);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }

    @RequestMapping("/sleep7")
    @Description("Call that takes 7 seconds to complete")
    public Map<String, Object> sleep7() throws InterruptedException {
        Thread.sleep(7000);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }

    @RequestMapping("/randomResponseTime/**")
    @Description("Waits for random time and then returns")
    public Map<String, Object> randomResponseTime() throws InterruptedException {
        double a = Math.random() * 10000;
        double fail = Math.random() * 10;

        Thread.sleep(Math.round(a));

        if ( fail < 2.0 ) {
            throw new RuntimeException();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }

    @RequestMapping("/fails")
    public void fails() throws Exception {
        throw new RuntimeException();
    }
}
