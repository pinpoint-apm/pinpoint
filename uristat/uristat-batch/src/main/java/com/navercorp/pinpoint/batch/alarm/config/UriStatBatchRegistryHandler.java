package com.navercorp.pinpoint.batch.alarm.config;

import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmHistory;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;
import com.navercorp.pinpoint.batch.alarm.vo.UriStatQueryParams;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class UriStatBatchRegistryHandler {

    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias("PinotAlarmKey", PinotAlarmKey.class);
        typeAliasRegistry.registerAlias("PinotAlarmHistory", PinotAlarmHistory.class);
        typeAliasRegistry.registerAlias("PinotAlarmRule", PinotAlarmRule.class);
        typeAliasRegistry.registerAlias("UriStatQueryParams", UriStatQueryParams.class);
    }

    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {

    }
}
