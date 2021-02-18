package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * https://github.com/pinpoint-apm/pinpoint/issues/7659
 */
@Component
public class ReactorNettyBugWorkaroundFilter implements IgnoreStatFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //    public static final ServiceType REACTOR_NETTY_CODE = ReactorNettyConstants.REACTOR_NETTY_CLIENT;
//    public static final ServiceType REACTOR_NETTY_INTERNAL_CODE = ReactorNettyConstants.REACTOR_NETTY_CLIENT_INTERNAL;
    public static final int REACTOR_NETTY_CODE = 9154;
    public static final int REACTOR_NETTY_INTERNAL_CODE = 9155;

    private final int[] filter = {REACTOR_NETTY_CODE, REACTOR_NETTY_INTERNAL_CODE};
    private final String[] invalidStrs = {"/", "?"};

    @Value("${collector.reactor-netty-bug-workaround:true}")
    private boolean enable = true;

    @Override
    public boolean filter(ServiceType calleeServiceType, String callerHost) {
        if (!enable) {
            return false;
        }
        if (StringUtils.isEmpty(callerHost)) {
            return false;
        }
        if (!filterServiceCode(calleeServiceType)) {
            return false;
        }

        if (!filterInvalidStr(callerHost)) {
            return false;
        }
        return true;
    }

    private boolean filterInvalidStr(String callerHost) {
        for (String invalidStr : invalidStrs) {
            if (!validStrCheck(callerHost, invalidStr)) {
                logger.debug("filter:{} {}", callerHost, invalidStr);
                return true;
            }
        }
        return false;
    }

    private boolean validStrCheck(String callHost, String invalidChar) {
        final int offset = callHost.indexOf(invalidChar);
        if (offset == -1) {
            return true;
        }
        return false;
    }


    private boolean filterServiceCode(ServiceType calleeServiceType) {
        for (int serviceCode : filter) {
            if (serviceCode == calleeServiceType.getCode()) {
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    public void log() {
        logger.info("enable:{}", enable);
    }
}

