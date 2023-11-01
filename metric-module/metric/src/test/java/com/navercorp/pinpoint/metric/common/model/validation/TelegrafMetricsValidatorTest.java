package com.navercorp.pinpoint.metric.common.model.validation;

import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TelegrafMetricsValidatorTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testMetrics() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();

        SystemMetric systemMetric = new SystemMetric("", "fieldname", "hostName", Collections.emptyList(), System.currentTimeMillis());
        Metrics metrics = new Metrics("tenantId", "hostGroupName", "hostName", List.of(systemMetric));

        Set<ConstraintViolation<Metrics>> result = validator.validate(metrics);

        assertThat(result).hasSize(1);

        ConstraintViolationException invalid_metric = new ConstraintViolationException("invalid metric", result);
        logger.debug("{}", invalid_metric.toString());
    }
}