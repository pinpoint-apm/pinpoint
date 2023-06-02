package com.navercorp.pinpoint.profiler.monitor.processor.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.monitor.processor.ConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.PropertiesKey;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import com.navercorp.pinpoint.profiler.sampler.SamplingRateSampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author dongdd
 * @description：采样率处理器
 */
public class SamplerConfigProcessor implements ConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Sampler sampler;
    public SamplerConfigProcessor(Sampler sampler){
        this. sampler = sampler;
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
                && null != sampler && sampler instanceof SamplingRateSampler
                && (properties.containsKey(PropertiesKey.SAMPLE_RATE.key) || properties.containsKey(PropertiesKey.SAMPLE_ENABLE.key))
        ){
            return true;
        }
        return false;
    }

    @Override
    public void resetConfig(String configStr) {
        try {
            Properties sampleProperties = ReSetConfigProcessorFactory.configStr2Properties(configStr);

            Integer samplingRate = sampleProperties.containsKey(PropertiesKey.SAMPLE_RATE.key) ? Integer.parseInt(""+sampleProperties.get(PropertiesKey.SAMPLE_RATE.key)) : null;
            Boolean sampling = sampleProperties.containsKey(PropertiesKey.SAMPLE_ENABLE.key) ? Boolean.parseBoolean(""+sampleProperties.get(PropertiesKey.SAMPLE_ENABLE.key)) : null;

            Field samplingRateField = sampler.getClass().getDeclaredField("samplingRate");
            if(samplingRate != null){
                if(samplingRateField != null){
                    samplingRateField.setAccessible(true);
                    Integer samplingRateOld = Integer.parseInt(samplingRateField.get(sampler)+"");
                    if(!samplingRate.equals(samplingRateOld) ){
                        samplingRateResult = samplingRate;
                        samplingRateField.set(sampler, samplingRate);
                    }
                }else{
                    logger.warn("SamplingRateSampler class samplingRate field is null, return!");
                }
            }
            if(sampling != null){
                Field samplingField = sampler.getClass().getDeclaredField("sampling");
                if(samplingField != null){
                    samplingField.setAccessible(true);
                    Boolean samplingOld = Boolean.parseBoolean(samplingField.get(sampler)+"");
                    if(!sampling.equals(samplingOld) ){
                        samplingResult = sampling;
                        samplingField.set(sampler, sampling);
                    }
                }else{
                    logger.warn("SamplingRateSampler class sampling field is null, return!");
                }
            }
        } catch (Exception e) {
            logger.error("resetConfig {}-{} error, configStr=[{}], e=[{}]!", PropertiesKey.SAMPLE_RATE.key, PropertiesKey.SAMPLE_ENABLE.key, configStr, e);
        }
    }

    private static Integer samplingRateResult;
    private static Boolean samplingResult;
    @Override
    public void resetProperties(Properties properties){
        if(samplingRateResult != null){
            properties.put(PropertiesKey.SAMPLE_RATE.key, samplingRateResult);
        }
        if(samplingResult != null) {
            properties.put(PropertiesKey.SAMPLE_ENABLE.key, samplingResult);
        }
    }
}
