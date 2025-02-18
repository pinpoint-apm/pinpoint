package com.navercorp.pinpoint.profiler.name;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ObjectNameResolver {
    ObjectName resolve();


    static ObjectNameProperty resolve(AgentProperties agentProperty,
                                       Function<AgentProperties, ObjectNameProperty> keyExtractor,
                                       Predicate<ObjectNameProperty> validator) {
        final ObjectNameProperty property = keyExtractor.apply(agentProperty);
        final String value = property.getValue();
        if (!StringUtils.hasLength(value)) {
            return null;
        }
        if (validator.test(property)) {
            return property;
        }
        return null;
    }
}
