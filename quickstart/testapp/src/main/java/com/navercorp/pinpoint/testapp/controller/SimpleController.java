package com.navercorp.pinpoint.testapp.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
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
    @Description("Call a webservice using jax ws client")
    public Map<String, Object> webserviceJAX() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        String response = "ok";
        
        try {
            StockQuote service = new StockQuote();
            StockQuoteSoap soap = service.getStockQuoteSoap();

            response = soap.getQuote("KO");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        map.put("message", response);
        return map;
    }
    
    @RequestMapping("/webserviceJAXRPC")
    @ResponseBody
    @Description("Call a webservice using jax rpc client")
    public Map<String, Object> webserviceAxis() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        String response = "ok";

        try {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace opN = fac.createOMNamespace("http://www.webserviceX.NET/", "");
            ServiceClient serviceClient = new ServiceClient();

            Options options = serviceClient.getOptions();
            EndpointReference targetEPR = new EndpointReference("http://www.webservicex.net/stockquote.asmx");
            options.setTo(targetEPR);
            options.setAction("http://www.webserviceX.NET/GetQuote");
            
            OMElement opGetQuote = fac.createOMElement("GetQuote", opN); 
            
            OMElement symbol = fac.createOMElement("symbol", opN); 
            OMText textNode = fac.createOMText("KO");
            symbol.addChild(textNode);
            
            opGetQuote.addChild(symbol);

            OMElement result = serviceClient.sendReceive(opGetQuote);

            response = result.getFirstElement().getText();
            System.out.println(response);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
        
        map.put("message", response);
        return map;
    }
    
    @RequestMapping("/webserviceSAAJ")
    @ResponseBody
    @Description("Call a webservice using SAAJ")
    public Map<String, Object> webserviceSaaj() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        String response = "ok";

        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection connection = soapConnectionFactory.createConnection();

            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage();
            message.getMimeHeaders().addHeader("SOAPAction", "http://www.webserviceX.NET/GetQuote");

            SOAPHeader header = message.getSOAPHeader();
            SOAPBody body = message.getSOAPBody();
            header.detachNode();
            
            QName bodyName = new QName("http://www.webserviceX.NET/", "GetQuote");
            SOAPBodyElement bodyElement = body.addBodyElement(bodyName);

            QName name = new QName("http://www.webserviceX.NET/", "symbol");
            SOAPElement symbol = bodyElement.addChildElement(name);
            symbol.addTextNode("KO");

            URL endpoint = new URL("http://www.webservicex.net/stockquote.asmx");
            SOAPMessage result = connection.call(message, endpoint);

            connection.close();

            SOAPBody soapBody = result.getSOAPBody();

            response = soapBody.getTextContent();
            System.out.print(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        map.put("message", response);
        return map;
    }

}
