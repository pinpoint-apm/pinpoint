package com.navercorp.pinpoint.common.server;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.config.AgentStatSerializeConfiguration;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.config.ApplicationStatSerializeConfiguration;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.config.SpanSerializeConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SpanSerializeConfiguration.class,

        AgentStatSerializeConfiguration.class,
        ApplicationStatSerializeConfiguration.class,

})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.common.server.bo.codec",
        "com.navercorp.pinpoint.common.server.dao.hbase.mapper",
})
public class CommonsHbaseConfiguration {
    @Bean
    public AgentStatDataPointCodec agentStatDataPointCodec() {
        return new AgentStatDataPointCodec();
    }


}
