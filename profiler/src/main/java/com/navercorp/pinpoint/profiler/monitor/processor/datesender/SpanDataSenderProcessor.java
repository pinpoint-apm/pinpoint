package com.navercorp.pinpoint.profiler.monitor.processor.datesender;

import com.navercorp.pinpoint.profiler.monitor.processor.ConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.PropertiesKey;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.RefreshStrategy;
import com.navercorp.pinpoint.profiler.sender.UdpDataSender;
import com.navercorp.pinpoint.rpc.client.DnsSocketAddressProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author dongdd
 * @description：UDP-span
 */
public class SpanDataSenderProcessor implements ConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private DataSender spanDataSender;

    public SpanDataSenderProcessor(DataSender spanDataSender){
        this.spanDataSender = spanDataSender;
    }
    @Override
    public boolean isReset(String configStr) {
        Properties properties = null;
        try {
            properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
        } catch (Exception e) {
            return false;
        }
        if(null != configStr && null!=properties &&
                (properties.containsKey(PropertiesKey.SPAN_SENDER_IP.key) || properties.containsKey(PropertiesKey.SPAN_SENDER_PORT.key))
        ){
            return true;
        }
        return false;
    }

    @Override
    public void resetConfig(String configStr) {
        try {
            Properties properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
            if(spanDataSender != null && properties != null){
                if(spanDataSender instanceof UdpDataSender){
                    Object hostNewObj = properties.get(PropertiesKey.SPAN_SENDER_IP.key);
                    Object portNewObj = properties.get(PropertiesKey.SPAN_SENDER_PORT.key);
                    Object hostOrigin = null;
                    Object portOrigin = null;
                    Field field = spanDataSender.getClass().getDeclaredField("socketAddressProvider");
                    if(field != null){
                        field.setAccessible(true);
                        Object socketAddressProviderObj = field.get(spanDataSender);
                        if(socketAddressProviderObj != null
                                &&socketAddressProviderObj instanceof RefreshStrategy){
                            Field socketAddressProviderField = socketAddressProviderObj.getClass().getDeclaredField("socketAddressProvider");
                            if(socketAddressProviderField != null){
                                socketAddressProviderField.setAccessible(true);
                                Object dnsSocketAddressProviderObj = socketAddressProviderField.get(socketAddressProviderObj);
                                if(dnsSocketAddressProviderObj != null && dnsSocketAddressProviderObj instanceof DnsSocketAddressProvider){
                                    Field host = dnsSocketAddressProviderObj.getClass().getDeclaredField("host");
                                    Field port = dnsSocketAddressProviderObj.getClass().getDeclaredField("port");
                                    if(host != null){
                                        host.setAccessible(true);
                                        hostOrigin = host.get(dnsSocketAddressProviderObj);
                                    }
                                    if(port != null){
                                        port.setAccessible(true);
                                        portOrigin = port.get(dnsSocketAddressProviderObj);
                                    }
                                }
                            }
                        }
                        String host = String.valueOf(ReSetConfigProcessorFactory.getHostOrPort(hostNewObj, hostOrigin));
                        Integer port = Integer.parseInt(ReSetConfigProcessorFactory.getHostOrPort(portNewObj, portOrigin)+"");
                        if(!host.equals(hostOrigin) || !(""+port).equals(portOrigin)){
                            hostResult = host;
                            portResult = port;
                            field.set(spanDataSender, new RefreshStrategy(new DnsSocketAddressProvider(host, port)));
                        }
                    }
                }else{
                    logger.warn("span sender is not UdpDataSender");
                }
            }
        } catch (Exception e) {
            logger.error("SpanDataSenderProcessor resetConfig {} error, configStr=[{}-{}], e=[{}]!", PropertiesKey.SPAN_SENDER_IP.key, PropertiesKey.SPAN_SENDER_PORT.key, configStr, e);
        }
    }

    private static String hostResult;
    private static Integer portResult;
    @Override
    public void resetProperties(Properties properties){
        if(hostResult != null){
            properties.put(PropertiesKey.SPAN_SENDER_IP.key, hostResult);
        }
        if(portResult != null) {
            properties.put(PropertiesKey.SPAN_SENDER_PORT.key, portResult);
        }
    }
}
