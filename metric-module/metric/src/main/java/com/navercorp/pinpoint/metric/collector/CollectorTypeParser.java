package com.navercorp.pinpoint.metric.collector;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectorTypeParser {

    public static final String COLLECTOR_TYPE_KEY = "pinpoint.collector.type";
    private final String key;

    public CollectorTypeParser() {
        this(COLLECTOR_TYPE_KEY);
    }

    public CollectorTypeParser(String key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    public TypeSet parse(String[] args) {
        ApplicationArguments arguments = new DefaultApplicationArguments(args);
        return parse(arguments);
    }

    public TypeSet parse(ApplicationArguments arguments) {
        List<String> optionValues = arguments.getOptionValues(key);
        if (CollectionUtils.isEmpty(optionValues)) {
            return new TypeSet(Set.of(CollectorType.ALL));
        }

        Set<CollectorType> collect = optionValues.stream()
                .flatMap(type -> StringUtils.tokenizeToStringList(type, ",").stream())
                .map(String::toUpperCase)
                .map(CollectorType::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new TypeSet(collect);
    }
}
