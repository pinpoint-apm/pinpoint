package com.nhn.pinpoint.web.alarm.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class SlowCountFilter extends AlarmCheckCountFilter {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ResponseTimeDataCollector dataCollector;

    public SlowCountFilter(DataCollector dataCollector, Rule rule) {
        super(rule, "");
        this.dataCollector = (ResponseTimeDataCollector)dataCollector;
        
    }
    
    @Override
    public void check() {
        logger.debug("{} check.", this.getClass().getSimpleName());
        dataCollector.collect();
        
        if (decideResult(dataCollector.getSlowCount())) {
            detected = true;
        } else {
            detected = false;
        }
    }

    @Override
    public String getDetectedValue() {
        return String.valueOf(dataCollector.getSlowCount());
    }
}
