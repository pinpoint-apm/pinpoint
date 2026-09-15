package com.navercorp.pinpoint.it.plugin.druid;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.alibaba:druid:[1.1.21,)", "com.h2database:h2:1.4.200"})
public class DruidJDK8IT extends DruidIT {

}