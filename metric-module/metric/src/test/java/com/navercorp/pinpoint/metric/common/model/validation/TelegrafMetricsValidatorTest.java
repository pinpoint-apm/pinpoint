package com.navercorp.pinpoint.metric.common.model.validation;

import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;

public class TelegrafMetricsValidatorTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testMetrics() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();

        SystemMetric systemMetric = new SystemMetric("", "fieldname", "hostName", Collections.emptyList(), System.currentTimeMillis());
        Metrics metrics = new Metrics("hostGroupName", "hostName", Collections.singletonList(systemMetric));

        Set<ConstraintViolation<Metrics>> result = validator.validate(metrics);

        Assert.assertEquals(1, result.size());

        ConstraintViolationException invalid_metric = new ConstraintViolationException("invalid metric", result);
        logger.debug("{}", invalid_metric.toString());
    }
}