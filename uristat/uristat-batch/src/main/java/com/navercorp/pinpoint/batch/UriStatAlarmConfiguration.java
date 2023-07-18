package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.batch.alarm.config.UriStatBatchDaoConfiguration;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({
        "classpath:job/applicationContext-uriAlarmJob.xml",
        "classpath:applicationContext-pinot-batch-dao-config.xml"
})
@Import({
        UriStatBatchDaoConfiguration.class,
        PinotConfiguration.class
})
public class UriStatAlarmConfiguration {
}
