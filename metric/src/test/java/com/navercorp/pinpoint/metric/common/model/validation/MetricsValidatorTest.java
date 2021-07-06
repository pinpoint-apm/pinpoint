package com.navercorp.pinpoint.metric.common.model.validation;


import com.navercorp.pinpoint.metric.common.model.Metrics;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class MetricsValidatorTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testMetrics() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();

        SystemMetric systemMetric = new SystemMetric("", "hostName", "fieldname", Collections.emptyList(), System.currentTimeMillis());
        Metrics metrics = new Metrics(Arrays.asList(systemMetric));

        Set<ConstraintViolation<Metrics>> result = validator.validate(metrics);

        Assert.assertEquals(1, result.size());

        ConstraintViolationException invalid_metric = new ConstraintViolationException("invalid metric", result);
        logger.debug("{}", invalid_metric.toString());
    }
}