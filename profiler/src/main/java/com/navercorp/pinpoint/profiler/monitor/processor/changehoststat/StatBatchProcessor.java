package com.navercorp.pinpoint.profiler.monitor.processor.changehoststat;

import com.navercorp.pinpoint.profiler.monitor.processor.ConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.PropertiesKey;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author dongdd
 */
public class StatBatchProcessor implements ConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Properties properties;

    public StatBatchProcessor(Properties properties){
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
        if(null != configStr && null!=properties
                && (properties.containsKey(PropertiesKey.STAT_BATCH_COUNR.key))
        ){
            return true;
        }
        return false;
    }

    @Override
    public void resetConfig(String configStr) {
        try {
            Properties properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
            
            Integer batchCountNew = properties.containsKey(PropertiesKey.STAT_BATCH_COUNR.key) ? Integer.parseInt(properties.get(PropertiesKey.STAT_BATCH_COUNR.key)+"") : null;
            Integer batchCountOld = Integer.parseInt(this.properties.get(PropertiesKey.STAT_BATCH_COUNR.key)+"");

            if(batchCountNew!=null && !batchCountNew.equals(batchCountOld)){
                this.properties.put(PropertiesKey.STAT_BATCH_COUNR.key, batchCountNew);
            }
        } catch (Exception e) {
            logger.error("StatBatchProcessor resetConfig {} error, configStr=[{}], e=[{}]!", PropertiesKey.STAT_BATCH_COUNR.key, configStr, e);
        }
    }
    @Override
    public void resetProperties(Properties properties){
        //resetConfig已经设置值到properties
       return;
    }
}
