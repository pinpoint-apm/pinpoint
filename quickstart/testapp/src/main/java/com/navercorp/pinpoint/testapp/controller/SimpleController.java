package com.navercorp.pinpoint.testapp.controller;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.testapp.util.Description;

import net.webservicex.StockQuote;
import net.webservicex.StockQuoteSoap;


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

    @RequestMapping("/webserviceJAXWS")
    @ResponseBody
    @Description("Call a webservice")
    public Map<String, Object> webserviceJAX() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        String response = "ok";
        
        try {
            StockQuote service = new StockQuote();
            System.out.println("Retrieving the port from the following service: " + service);
            StockQuoteSoap soap = service.getStockQuoteSoap();
            System.out.println("Invoking the sayHello operation on the port.");

            response = soap.getQuote("KO");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
}
        
        map.put("message", response);
        return map;
    }
    
    @RequestMapping("/webserviceAxis2RPC")
    @ResponseBody
    @Description("Call a webservice")
    public Map<String, Object> webserviceAxis() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        String response = "ok";

        try {
            RPCServiceClient serviceClient = new RPCServiceClient();

            Options options = serviceClient.getOptions();

            EndpointReference targetEPR = new EndpointReference(
                    "http://www.webservicex.net/stockquote.asmx?WSDL");
            options.setTo(targetEPR);
            options.setAction("urn:GetQuote");

            QName opGetQuote = new QName("http://ws.axis2.apache.org", "GetQuote", "req");

            OMElement result = serviceClient.invokeBlocking(opGetQuote, new Object[] { "KO" });

            response = result.getFirstElement().getText();
            System.out.println(response);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
        
        map.put("message", response);
        return map;
    }

}
