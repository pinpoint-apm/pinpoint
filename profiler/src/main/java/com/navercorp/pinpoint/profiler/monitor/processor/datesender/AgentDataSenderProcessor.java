package com.navercorp.pinpoint.profiler.monitor.processor.datesender;

import com.navercorp.pinpoint.profiler.monitor.processor.ConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.PropertiesKey;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.client.DefaultPinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.DnsSocketAddressProvider;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author dongdd
 * @description：TCP-agent
 */
public class AgentDataSenderProcessor implements ConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private DataSender agentDataSender;

    public AgentDataSenderProcessor(DataSender agentDataSender){
        this.agentDataSender = agentDataSender;
    }
    @Override
    public boolean isReset(String configStr) {
        Properties properties = null;
        try {
            properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
        } catch (Exception e) {
            return false;
        }
        if(null != configStr && null!=properties
                && (properties.containsKey(PropertiesKey.AGENT_SENDER_IP.key) || properties.containsKey(PropertiesKey.AGENT_SENDER_PORT.key))
        ){
            return true;
        }
        return false;
    }

    @Override
    public void resetConfig(String configStr) {
        try {
            Properties properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
            if(agentDataSender != null && properties != null){
                if(agentDataSender instanceof TcpDataSender){
                    Object hostNewObj = properties.get(PropertiesKey.AGENT_SENDER_IP.key);
                    Object portNewObj = properties.get(PropertiesKey.AGENT_SENDER_PORT.key);
                    Object hostOrigin = null;
                    Object portOrigin = null;
                    Field field = agentDataSender.getClass().getDeclaredField("clientProvider");
                    if(field != null){
                        field.setAccessible(true);
                        Object clientProviderObj = field.get(agentDataSender);
                        ClientFactoryUtils.PinpointClientProvider clientProvider = null;
                        if(clientProviderObj != null
                                &&clientProviderObj instanceof ClientFactoryUtils.PinpointClientProvider){
                            clientProvider = (ClientFactoryUtils.PinpointClientProvider)clientProviderObj;
                            String addressAsString = clientProvider.getAddressAsString();
                            hostOrigin = addressAsString.split(":")[0];
                            portOrigin = addressAsString.split(":")[1];
                        }else{
                            logger.error("clientProviderObj is error, return!");
                            return;
                        }
                        String host = String.valueOf(ReSetConfigProcessorFactory.getHostOrPort(hostNewObj, hostOrigin));
                        Integer port = Integer.parseInt(ReSetConfigProcessorFactory.getHostOrPort(portNewObj, portOrigin)+"");
                        if((!host.equals(hostOrigin) || !(""+port).equals(portOrigin))
                                && clientProvider!=null){
                            PinpointClient pinpointClient = clientProvider.get(host, port);
                            if(pinpointClient != null && pinpointClient.isConnected()){
                                hostResult = host;
                                portResult = port;
                                Field fieldClient = agentDataSender.getClass().getDeclaredField("client");
                                if(fieldClient != null){
                                    fieldClient.setAccessible(true);
                                    fieldClient.set(agentDataSender, pinpointClient);
                                    Field clientFactoryField = clientProvider.getClass().getDeclaredField("clientFactory");
                                    if(clientFactoryField != null){
                                        clientFactoryField.setAccessible(true);
                                        Object clientFactoryObj = clientFactoryField.get(clientProvider);
                                        if(clientFactoryObj != null && clientFactoryObj instanceof DefaultPinpointClientFactory){
                                            Field socketAddressScheduledProviderField = clientFactoryObj.getClass().getDeclaredField("socketAddressScheduledProvider");
                                            socketAddressScheduledProviderField.setAccessible(true);
                                            Object socketAddressScheduledProviderObj = socketAddressScheduledProviderField.get(clientFactoryObj);
                                            if(socketAddressScheduledProviderObj != null && socketAddressScheduledProviderObj instanceof DnsSocketAddressProvider){
                                                Field hostScheduled = socketAddressScheduledProviderObj.getClass().getDeclaredField("host");
                                                Field portScheduled = socketAddressScheduledProviderObj.getClass().getDeclaredField("port");
                                                hostScheduled.setAccessible(true);
                                                portScheduled.setAccessible(true);
                                                hostScheduled.set(socketAddressScheduledProviderObj, host);
                                                portScheduled.set(socketAddressScheduledProviderObj, port);
                                            }
                                        }
                                    }else{
                                        logger.warn("clientProvider class clientFactory field is null, return!");
                                    }
                                }else{
                                    logger.warn("agentDataSender class client field is null, return!");
                                }
                            }else{
                                logger.warn("tcp connect fail!{}:{}", host, port);
                            }
                        }
                    }else{
                        logger.warn("TcpDataSender class clientProvider field is null, return!");
                    }
                }else{
                    logger.info("agent sender is not TcpDataSender");
                }
            }
        } catch (Exception e) {
            logger.error("agentDataSenderProcessor resetConfig {} error, configStr=[{}-{}], e=[{}]!", PropertiesKey.AGENT_SENDER_IP.key, PropertiesKey.AGENT_SENDER_PORT.key, configStr, e);
        }
    }

    private static String hostResult;
    private static Integer portResult;
    @Override
    public void resetProperties(Properties properties){
        if(hostResult != null){
            properties.put(PropertiesKey.AGENT_SENDER_IP.key, hostResult);
        }
        if(portResult != null) {
            properties.put(PropertiesKey.AGENT_SENDER_PORT.key, portResult);
        }
    }
}
