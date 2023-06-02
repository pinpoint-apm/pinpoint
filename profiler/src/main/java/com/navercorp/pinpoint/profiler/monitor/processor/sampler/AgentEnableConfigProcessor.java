package com.navercorp.pinpoint.profiler.monitor.processor.sampler;

import com.navercorp.pinpoint.profiler.monitor.processor.ConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.PropertiesKey;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author dongdd
 * @description：采样率处理器
 */
public class AgentEnableConfigProcessor implements ConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Properties properties;
    public AgentEnableConfigProcessor(Properties properties){
        this.properties = properties;
    }
    @Override
    public boolean isReset(String configStr) {
        Properties properties = null;
        try {
            properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
        } catch (Exception e) {
            return false;
        }
        if(null != configStr &&  null!=properties
                && (properties.containsKey(PropertiesKey.AGENT_ENABLE.key))
        ){
            return true;
        }
        return false;
    }

    @Override
    public void resetConfig(String configStr) {
        try {
            Properties sampleProperties = ReSetConfigProcessorFactory.configStr2Properties(configStr);

            Boolean agentEnableNew = sampleProperties.containsKey(PropertiesKey.AGENT_ENABLE.key) ? Boolean.parseBoolean(sampleProperties.get(PropertiesKey.AGENT_ENABLE.key)+"") : null;
            Boolean agentEnableOld = properties.containsKey(PropertiesKey.AGENT_ENABLE.key) ? Boolean.parseBoolean(properties.get(PropertiesKey.AGENT_ENABLE.key)+"") : null;

            if(agentEnableNew != null && !agentEnableNew.equals(agentEnableOld)){
                ReSetConfigProcessorFactory.setEnableCollect(agentEnableNew.booleanValue());
                agentEnableNewValue = agentEnableNew.booleanValue();
            }

        } catch (Exception e) {
            logger.error("AgentEnableConfigProcessor resetConfig {} error, configStr=[{}], e=[{}]!", PropertiesKey.AGENT_ENABLE.key, configStr, e);
        }
    }
    private Boolean agentEnableNewValue;
    @Override
    public void resetProperties(Properties properties){
        if(agentEnableNewValue!=null){
            properties.put(PropertiesKey.AGENT_ENABLE.key, agentEnableNewValue.booleanValue());
        }
    }
}
