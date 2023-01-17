package com.navercorp.pinpoint.bootstrap.util.argument;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Validation {
    private final PLogger logger;
    private final List<Predicate> list = new ArrayList<>(4);
    private int min;

    public Validation(PLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public Validation addArgument(Class<?> clazz, int index) {
        this.list.add(new ClassPredicate(logger, clazz, index));
        return this;
    }

    public Validation addPredicate(Predicate predicate) {
        Objects.requireNonNull(predicate, "predicate");

        this.list.add(predicate);
        return this;
    }

    public Validation minArgsSize(int min) {
        this.min = min;
        return this;
    }

    public Validator build() {
        List<Predicate> list = new ArrayList<>(this.list);
        return new ArgumentValidator(list, min);
    }

}
