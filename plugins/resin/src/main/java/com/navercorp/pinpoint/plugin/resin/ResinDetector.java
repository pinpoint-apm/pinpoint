package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.resolver.condition.ClassResourceCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * 
 * @author huangpengjie@fang.com
 *
 */
public class ResinDetector {

    private static final String DEFAULT_EXPECTED_MAIN_CLASS = "com.caucho.server.resin.Resin";

    private static final String REQUIRED_CLASS = DEFAULT_EXPECTED_MAIN_CLASS;

    private final String expectedMainClass;

    public ResinDetector(String expectedMainClass) {
        this.expectedMainClass = expectedMainClass;
    }

    public boolean detect() {
        if (StringUtils.hasLength(expectedMainClass)) {
            return MainClassCondition.INSTANCE.check(expectedMainClass) && ClassResourceCondition.INSTANCE.check(REQUIRED_CLASS);
        }
        return ClassResourceCondition.INSTANCE.check(REQUIRED_CLASS);
    }

}
