package com.navercorp.pinpoint.common.server.uid;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CaffeineCacheSpecTest {

    @Test
    public void BuilderTest() {

        Map<String, String> spec = Map.of(
                "maximumSize", "100",
                "initialCapacity", "10",
                "expireAfterWrite", "10m",
                "recordStats", ""
        );

        CaffeineCacheSpec caffeineCacheSpec = new CaffeineCacheSpec();
        caffeineCacheSpec.setSpec(spec);
        String specification = caffeineCacheSpec.getSpecification();

        CaffeineSpec parse = CaffeineSpec.parse(specification);
        Caffeine<Object, Object> caffeine = Caffeine.from(parse);

        caffeine.build();
    }

    @Test
    public void WrongInputTest() {
        Map<String, String> spec = Map.of(
                "wrongKey1", "100",
                "wrongKey2", "10"
        );
        CaffeineCacheSpec caffeineCacheSpec = new CaffeineCacheSpec();
        caffeineCacheSpec.setSpec(spec);
        String specification = caffeineCacheSpec.getSpecification();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CaffeineSpec.parse(specification);
        });
    }

}
