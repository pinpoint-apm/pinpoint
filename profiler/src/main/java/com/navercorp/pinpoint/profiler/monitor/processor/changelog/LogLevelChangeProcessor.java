package com.navercorp.pinpoint.profiler.monitor.processor.changelog;

import com.navercorp.pinpoint.profiler.monitor.processor.ConfigProcessor;
import com.navercorp.pinpoint.profiler.monitor.processor.PropertiesKey;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author zy
 */
public class LogLevelChangeProcessor implements ConfigProcessor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Properties properties;
    private static final String WANT_PACKAGE ="com.navercorp.pinpoint";
    private static final String NACOS_PACKAGE ="com.alibaba.nacos";
    public LogLevelChangeProcessor(Properties properties){
        this.properties = properties;
    }
    public static final List<String> logKeyList = new ArrayList<String>(){{
        add(PropertiesKey.LOG_LEVEL_GLOBAL.key);
        add(PropertiesKey.LOG_LEVEL_PINPOINT.key);
        add(PropertiesKey.LOG_LEVEL_NACOS.key);
    }};
    @Override
    public boolean isReset(String configStr) {
        Properties properties = null;
        try {
            properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
        } catch (Exception e) {
            return false;
        }
        if(null != configStr && null!=properties
                && isContainerLogLevelKey(properties)
        ){
            return true;
        }
        return false;
    }
    private boolean isContainerLogLevelKey(Properties properties){
        for(String keySet : logKeyList){
            if(properties.containsKey(keySet)){
                return true;
            }
        }
        return false;
    }
    public void resetConfig(Properties properties){
        try {
            String logGlobalLevel = properties.containsKey(PropertiesKey.LOG_LEVEL_GLOBAL.key) ? String.valueOf(properties.get(PropertiesKey.LOG_LEVEL_GLOBAL.key)):null;
            String logPinpointLevel = properties.containsKey(PropertiesKey.LOG_LEVEL_PINPOINT.key) ? String.valueOf(properties.get(PropertiesKey.LOG_LEVEL_PINPOINT.key)):null;
            String logNacosLevel = properties.containsKey(PropertiesKey.LOG_LEVEL_NACOS.key) ? String.valueOf(properties.get(PropertiesKey.LOG_LEVEL_NACOS.key)):null;

            if(logGlobalLevel!=null&&!"".equals(logGlobalLevel)){
                changeLogLevel(logGlobalLevel);
            }
            if(logPinpointLevel!=null&&!"".equals(logPinpointLevel)){
                changeLogLevel(logPinpointLevel,WANT_PACKAGE);
            }
            if(logNacosLevel!=null&&!"".equals(logNacosLevel)){
                changeLogLevel(logNacosLevel,NACOS_PACKAGE);
            }
        } catch (Exception e) {
            logger.error("LogLevelChangeProcessor resetConfig {} error, configStr=[properties], e=[{}]!", PropertiesKey.LOG_LEVEL_GLOBAL.key, e);
        }
    }

    @Override
    public void resetConfig(String configStr) {
        try {
            Properties properties = ReSetConfigProcessorFactory.configStr2Properties(configStr);
            resetConfig(properties);
        } catch (Exception e) {
            logger.error("LogLevelChangeProcessor resetConfig {} error, configStr=[{}], e=[{}]!", PropertiesKey.LOG_LEVEL_GLOBAL.key, configStr, e);
        }
    }
    @Override
    public void resetProperties(Properties properties){
        setLogLevel(properties);
        return;
    }
    public static void setLogLevel(Properties properties){
        try {
            Level rootLevel = LogManager.getRootLogger().getLevel();
            if(rootLevel!=null){
                properties.put(PropertiesKey.LOG_LEVEL_GLOBAL.key, rootLevel.toString());
            }
            Level pinpointLevel = LogManager.getLogger(WANT_PACKAGE).getLevel();
            if(pinpointLevel!=null){
                properties.put(PropertiesKey.LOG_LEVEL_PINPOINT.key, pinpointLevel.toString());
            }
            Level nacosLevel = LogManager.getLogger(NACOS_PACKAGE).getLevel();
            if(nacosLevel!=null){
                properties.put(PropertiesKey.LOG_LEVEL_NACOS.key, nacosLevel.toString());
            }
        } catch (Exception e) {
        }
    }

    public void changeLogLevel(String wantLevel){
        try {
            Level level = Level.toLevel(wantLevel);
            LogManager.getRootLogger().setLevel(level);
        } catch (Exception e) {
            logger.error("LogLevelChangeProcessor resetConfig  {} error, configStr=[{}], e=[{}]!", PropertiesKey.LOG_LEVEL_GLOBAL.key, wantLevel, e);
        }
    }

    public void changeLogLevel(String wantLevel,String wantPackage){
        try {
            Level level = Level.toLevel(wantLevel);
            LogManager.getLogger(wantPackage).setLevel(level);
        } catch (Exception e) {
            logger.error("LogLevelChangeProcessor resetConfig  {} error, configStr=[{}], e=[{}]!", PropertiesKey.LOG_LEVEL_PINPOINT.key, wantLevel, e);
        }
    }
}
