package com.navercorp.pinpoint.common.server;

import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.config.SpanSerializeConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SpanSerializeConfiguration.class,
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.common.server.bo.codec",
        "com.navercorp.pinpoint.common.server.dao.hbase.mapper",
})
public class CommonsHbaseConfiguration {

}
