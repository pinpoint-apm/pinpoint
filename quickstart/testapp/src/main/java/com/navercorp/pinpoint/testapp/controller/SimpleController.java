package com.navercorp.pinpoint.testapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.util.Description;

import sample.axisversion.Version;
import sample.axisversion.VersionPortType;

/**
 * @author koo.taejin
 */
@Controller
public class SimpleController {

    @RequestMapping("/getCurrentTimestamp")
    @ResponseBody
    @Description("Returns the server's current timestamp.")
    public Map<String, Object> getCurrentTimestamp() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("getCurrentTimestamp", System.currentTimeMillis());

        return map;
    }

    @RequestMapping("/sleep3")
    @ResponseBody
    @Description("Call that takes 3 seconds to complete.")
    public Map<String, Object> sleep3() throws InterruptedException {
        Thread.sleep(3000);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }

    @RequestMapping("/sleep5")
    @ResponseBody
    @Description("Call that takes 5 seconds to complete")
    public Map<String, Object> sleep5() throws InterruptedException {
        Thread.sleep(5000);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }

    @RequestMapping("/sleep7")
    @ResponseBody
    @Description("Call that takes 7 seconds to complete")
    public Map<String, Object> sleep7() throws InterruptedException {
        Thread.sleep(7000);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "ok");

        return map;
    }
    
    @RequestMapping("/webservice")
    @ResponseBody
    @Description("Call a webservice")
    public Map<String, Object> webservice() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        String response = "ok";
        
//        try {
//            RPCServiceClient serviceClient = new RPCServiceClient();
//
//            Options options = serviceClient.getOptions();
//
//            EndpointReference targetEPR = new EndpointReference(
//                    "http://52.66.130.230:8080/axis2/services/listServices");
//            options.setTo(targetEPR);
//            options.setAction("urn:getVersion");
//
//            QName opGetVersion = 
//                new QName("http://ws.axis2.apache.org", "getVersion", "req");
//            
//            OMElement result = serviceClient.invokeBlocking(opGetVersion, new Object[]{null});
//            
//            response = result.getFirstElement().getText();
//            
//        } catch (AxisFault e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        try {
            Version service = new Version();
            System.out.println("Retrieving the port from the following service: " + service);
            VersionPortType port = service.getVersionHttpSoap11Endpoint();
            System.out.println("Invoking the sayHello operation on the port.");

            response = port.getVersion();
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        map.put("message", response);
        return map;
    }

}
