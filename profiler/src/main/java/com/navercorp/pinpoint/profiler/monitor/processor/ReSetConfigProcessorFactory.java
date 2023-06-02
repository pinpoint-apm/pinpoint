package com.navercorp.pinpoint.profiler.monitor.processor;


import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.monitor.processor.changehoststat.StatBatchProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.changelog.LogLevelChangeProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.datesender.AgentDataSenderProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.datesender.SpanDataSenderProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.datesender.StatDataSenderProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.sampler.AgentEnableConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.sampler.SamplerConfigProcessor;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author dongdd
 * @description：
 */
public class ReSetConfigProcessorFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final Set<ConfigProcessor> configProcessorSets = new HashSet<ConfigProcessor>();
    private Properties properties;
    private static boolean agentEnable = Boolean.TRUE;

    public ReSetConfigProcessorFactory(ProfilerConfig profilerConfig, Sampler sampler
            , DataSender statDataSender, DataSender spanDataSender, DataSender tcpDataSender
            , TraceContext traceContext
            , StorageFactory storageFactory) {
        properties = ((DefaultProfilerConfig)profilerConfig).getProperties();
        try {
            agentEnable = properties == null ?
                    Boolean.TRUE :
                    properties.containsKey(PropertiesKey.AGENT_ENABLE.key) ?
                            Boolean.parseBoolean(properties.get(PropertiesKey.AGENT_ENABLE.key)+"") : Boolean.TRUE;
        } catch (Exception e) {
        }
        //add new ConfigProcessor
        initSampleConfigProcessor(sampler);
        initStatDataSenderProcessor(statDataSender);
        initSpanDataSenderProcessor(spanDataSender);
        initAgentDataSenderProcessor(tcpDataSender);
        initAgentEnableConfigProcessor(properties);
        initStatBatchProcessor(properties);
        initLogLevelChangeProcessor(properties);
        if (logger.isDebugEnabled()) {
            logger.debug("Successful initialization ReSetConfigProcessorFactory, configProcessorSets.size: {}", configProcessorSets.size());
        }
    }
    public void dealConfigInfo(String configStr){
        for(ConfigProcessor item : configProcessorSets){
            if(item.isReset(configStr)){
                item.resetConfig(configStr);
                item.resetProperties(properties);
            }
        }
    }

    /**
     * 添加处理器
     * @param sampler
     */
    private void initSampleConfigProcessor(Sampler sampler){
        SamplerConfigProcessor samplerConfigProcessor = new SamplerConfigProcessor(sampler);
        configProcessorSets.add(samplerConfigProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add SamplerConfigProcessor successfully");
        }
    }
    private void initStatDataSenderProcessor(DataSender statDataSender){
        StatDataSenderProcessor statDataSenderProcessor = new StatDataSenderProcessor(statDataSender);
        configProcessorSets.add(statDataSenderProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add StatDataSenderProcessor successfully");
        }
    }
    private void initSpanDataSenderProcessor(DataSender spanDataSender){
        SpanDataSenderProcessor spanDataSenderProcessor = new SpanDataSenderProcessor(spanDataSender);
        configProcessorSets.add(spanDataSenderProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add SpanDataSenderProcessor successfully");
        }
    }
    private void initAgentDataSenderProcessor(DataSender agentDataSender) {
        AgentDataSenderProcessor agentDataSenderProcessor = new AgentDataSenderProcessor(agentDataSender);
        configProcessorSets.add(agentDataSenderProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add AgentDataSenderProcessor successfully");
        }
    }
    private void initAgentEnableConfigProcessor(Properties properties){
        AgentEnableConfigProcessor agentEnableConfigProcessor = new AgentEnableConfigProcessor(properties);
        configProcessorSets.add(agentEnableConfigProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add AgentEnableConfigProcessor successfully");
        }
    }
    private void initStatBatchProcessor(Properties properties){
        StatBatchProcessor statBatchProcessor = new StatBatchProcessor(properties);
        configProcessorSets.add(statBatchProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add StatBatchProcessor successfully");
        }
    }

    private void initLogLevelChangeProcessor(Properties properties){
        LogLevelChangeProcessor logLevelChangeProcessor = new LogLevelChangeProcessor(properties);
        configProcessorSets.add(logLevelChangeProcessor);
        if (logger.isDebugEnabled()) {
            logger.debug("add logLevelChangeProcessor successfully");
        }
    }

    public static Properties configStr2Properties(String configStr) throws IOException {
        Properties result = new Properties();
        result.load(new ByteArrayInputStream(configStr.getBytes()));
        return result;
    }

    public static Object getHostOrPort(Object newObj, Object originObj){
        if(newObj != null){
            return newObj;
        }else if(newObj == null && originObj!=null){
            return originObj;
        }else{
            throw new RuntimeException("getHostOrPort error,newObj="+newObj+" originObj="+originObj);
        }
    }

    public static boolean isEnableCollect(ProfilerConfig profilerConfig){
        return agentEnable;
    }
    public static void setEnableCollect(boolean agentEnableNew){
        agentEnable = agentEnableNew;
    }

    public static Map<String, Field> fieldToMap(Field[] traceContextFields){
        Map<String, Field> objectObjectHashMap = null;
        if(traceContextFields != null && traceContextFields.length>0){
            objectObjectHashMap = new HashMap<String, Field>(traceContextFields.length);
            for(Field item : traceContextFields){
                objectObjectHashMap.put(item.getName(), item);
            }
        }
        return objectObjectHashMap;
    }
}
