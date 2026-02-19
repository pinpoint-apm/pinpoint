package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.batch.alarm.config.UriStatBatchDaoConfiguration;
import com.navercorp.pinpoint.batch.config.PinotBatchDaoConfig;
import com.navercorp.pinpoint.batch.config.UriAlarmJobConfig;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        UriStatBatchDaoConfiguration.class,
        PinotConfiguration.class,

        PinotBatchDaoConfig.class,
        UriAlarmJobConfig.class
})
public class UriStatAlarmConfiguration {
}
