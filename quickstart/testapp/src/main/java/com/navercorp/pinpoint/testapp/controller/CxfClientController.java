/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.testapp.controller;

import java.util.HashMap;
import java.util.Map;

import net.webservicex.GlobalWeather;
import net.webservicex.GlobalWeatherSoap;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.util.Description;
import com.w3schools.xml.TempConvert;
import com.w3schools.xml.TempConvertSoap;

/**
 * @author barney
 *
 */
@Controller
@RequestMapping("/cxfclient")
public class CxfClientController {

    private static long CONNECTION_TIMEOUT = 4000;

    private static long RECEIVE_TIMEOUT = 10000;

    @RequestMapping("weather")
    @ResponseBody
    @Description("Cxf GlobalWeather(http://www.webservicex.net/globalweather.asmx?wsdl)")
    public String weather(@RequestParam(defaultValue = "seoul") String city) throws Exception {
        GlobalWeather globalWeather = new GlobalWeather();
        GlobalWeatherSoap globalWeatherSoap = globalWeather.getGlobalWeatherSoap12();
        setClientPolicy(globalWeatherSoap);

        return globalWeatherSoap.getWeather(city, "");
    }

    @RequestMapping("tempconvert")
    @ResponseBody
    @Description("Cxf TempConvert (http://www.w3schools.com/xml/tempconvert.asmx?wsdl)")
    public Map<String, String> tempconvert(@RequestParam(defaultValue = "100") String celsius) throws Exception {
        TempConvert tempConvert = new TempConvert();
        TempConvertSoap tempConvertSoap = tempConvert.getTempConvertSoap12();
        setClientPolicy(tempConvertSoap);

        String fahrenheit = tempConvertSoap.celsiusToFahrenheit(celsius);
        Map<String, String> result = new HashMap<String, String>();
        result.put("celsius", celsius);
        result.put("fahrenheit", fahrenheit);
        return result;
    }

    private void setClientPolicy(Object obj) {
        Client client = ClientProxy.getClient(obj);
        if (client != null) {
            client.getInInterceptors().add(new LoggingInInterceptor());
            client.getOutInterceptors().add(new LoggingOutInterceptor());

            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setConnectionTimeout(CONNECTION_TIMEOUT);
            policy.setReceiveTimeout(RECEIVE_TIMEOUT);
            policy.setAllowChunking(true);
            conduit.setClient(policy);
        }
    }
}
