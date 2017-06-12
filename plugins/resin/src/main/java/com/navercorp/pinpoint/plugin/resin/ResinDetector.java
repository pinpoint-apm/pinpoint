package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * 
 * @author huangpengjie@fang.com
 *
 */
public class ResinDetector implements ApplicationTypeDetector {

    private static final String DEFAULT_BOOTSTRAP_MAIN = "com.caucho.server.resin.Resin";

    private static final String REQUIRED_CLASS = DEFAULT_BOOTSTRAP_MAIN;

    private final String bootstrapMains;

    public ResinDetector(String bootstrapMains) {
        this.bootstrapMains = bootstrapMains;
    }

    @Override
    public ServiceType getApplicationType() {
        return ResinConstants.RESIN;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        if (StringUtils.hasLength(bootstrapMains)) {
            return provider.checkMainClass(bootstrapMains) && provider.checkForClass(REQUIRED_CLASS);
        }
        return provider.checkForClass(REQUIRED_CLASS);
    }

}
