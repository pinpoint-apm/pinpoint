package com.navercorp.pinpoint.profiler.name;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class ObjectNameFilter {
    private final List<AgentProperties> agentPropertyList;

    public ObjectNameFilter(List<AgentProperties> agentPropertyList) {
        this.agentPropertyList = Objects.requireNonNull(agentPropertyList, "agentPropertyList");
    }

    public ObjectNameProperty resolve(Function<AgentProperties, ObjectNameProperty> keyExtractor,
                                       Predicate<ObjectNameProperty> validator) {
        for (AgentProperties agentProperty : agentPropertyList) {
            final ObjectNameProperty property = keyExtractor.apply(agentProperty);
            final String value = property.getValue();
            if (!StringUtils.hasLength(value)) {
                continue;
            }
            if (validator.test(property)) {
                return property;
            }
        }
        return new ObjectNameProperty("Undefined", null, null, null);
    }
}
