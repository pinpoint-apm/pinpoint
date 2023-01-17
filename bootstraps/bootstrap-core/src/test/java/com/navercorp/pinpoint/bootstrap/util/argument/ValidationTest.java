package com.navercorp.pinpoint.bootstrap.util.argument;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
class ValidationTest {

    private final Validator validator = newArgumentValidator();

    public Validator newArgumentValidator() {
        PLogger logger = PLoggerFactory.getLogger(this.getClass());

        Validation validation = new Validation(logger);
        validation.addArgument(String.class, 0);
        validation.addArgument(Long.class, 1);
        return validation.build();
    }


    @Test
    void valid() {
        Object[] arguments = new Object[2];
        arguments[0] = "abc";
        arguments[1] = 123L;
        Assertions.assertTrue(validator.validate(arguments));
    }

    @Test
    void empty() {
        PLogger logger = PLoggerFactory.getLogger(this.getClass());

        Validation validation = new Validation(logger);
        Validator validator = validation.build();

        Assertions.assertTrue(validator.validate(new Object[0]));
        Assertions.assertTrue(validator.validate(new Object[2]));
    }

    @Test
    void predicate() {
        PLogger logger = PLoggerFactory.getLogger(this.getClass());

        Validation validation = new Validation(logger);
        validation.addPredicate(new Predicate() {
            @Override
            public boolean test(Object[] args) {
                Object arg = args[index()];
                return arg instanceof String;
            }

            @Override
            public int index() {
                return 0;
            }
        });
        Validator validator = validation.build();

        Assertions.assertFalse(validator.validate(new Object[]{1}));
        Assertions.assertTrue(validator.validate(new Object[]{"abc"}));
    }

    @Test
    void valid_boundary_check() {

        Assertions.assertFalse(validator.validate(null));
        Assertions.assertFalse(validator.validate(new Object[0]));
        Assertions.assertFalse(validator.validate(new Object[1]));

        Object[] arguments = new Object[10];
        arguments[0] = "abc";
        arguments[1] = 123L;
        Assertions.assertTrue(validator.validate(arguments));
    }

    @Test
    void valid_fail1() {
        Object[] arguments = new Object[2];
        arguments[0] = "abc";
        arguments[1] = new Object();
        Assertions.assertFalse(validator.validate(arguments));
    }


    @Test
    void valid_fail2() {
        Object[] arguments = new Object[2];
        arguments[0] = new Object();
        arguments[1] = 123L;
        Assertions.assertFalse(validator.validate(arguments));
    }

    @Test
    void valid_min() {
        PLogger logger = PLoggerFactory.getLogger(this.getClass());

        Validation validation = new Validation(logger);
        validation.addArgument(String.class, 0);
        validation.addArgument(Long.class, 1);
        validation.minArgsSize(3);
        Validator validator = validation.build();

        Object[] arguments1 = new Object[2];
        arguments1[0] = "abc";
        arguments1[1] = 123L;
        Assertions.assertFalse(validator.validate(arguments1));

        Object[] arguments2 = new Object[5];
        arguments2[0] = "abc";
        arguments2[1] = 123L;
        Assertions.assertTrue(validator.validate(arguments2));


    }
}