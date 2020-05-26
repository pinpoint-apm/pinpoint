package com.navercorp.pinpoint.plugin.druid;

import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;

@Dependency({"com.alibaba:druid:[1.1.21,)", "com.h2database:h2:1.4.191"})
@JvmVersion(8)
public class DruidJDK8IT extends DruidIT {

}