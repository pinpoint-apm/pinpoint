package com.navercorp.pinpoint.profiler.name.v4;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameProperty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdValidatorV4Test {

    private final IdValidatorV4 validator = new IdValidatorV4();

    private static ObjectNameProperty serviceName(String value) {
        return new ObjectNameProperty(ObjectName.SERVICE_NAME, value, IdSourceType.SYSTEM, null);
    }

    @Test
    void serviceName_minLength() {
        assertThat(PinpointConstants.SERVICE_NAME_MIN_LEN).isEqualTo(3);

        assertThat(validator.validateServiceName(serviceName("ab"))).isFalse();
        assertThat(validator.validateServiceName(serviceName("abc"))).isTrue();
        assertThat(validator.validateServiceName(serviceName(ObjectName.DEFAULT_SERVICE_NAME))).isTrue();
    }

    @Test
    void serviceName_empty() {
        assertThat(validator.validateServiceName(null)).isFalse();
        assertThat(validator.validateServiceName(serviceName(null))).isFalse();
        assertThat(validator.validateServiceName(serviceName(""))).isFalse();
    }

    @Test
    void serviceName_pattern_andMaxLength() {
        assertThat(validator.validateServiceName(serviceName("a b"))).isFalse();
        assertThat(validator.validateServiceName(serviceName("svc.name-1_x"))).isTrue();

        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i <= PinpointConstants.SERVICE_NAME_MAX_LEN; i++) {
            tooLong.append('a');
        }
        assertThat(validator.validateServiceName(serviceName(tooLong.toString()))).isFalse();
    }

    @Test
    void applicationName_hasNoMinLength() {
        assertThat(validator.validateApplicationName(new ObjectNameProperty(ObjectName.APPLICATION_NAME, "a", IdSourceType.SYSTEM, null))).isTrue();
    }
}
