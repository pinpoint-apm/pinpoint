package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmDataSourceRegistryTest {

    @Test
    void collectsEveryProvidersDataSources() {
        AlarmDataSourceRegistry registry = new AlarmDataSourceRegistry(
                List.of(provider(dataSource("A")), provider(dataSource("B"), dataSource("C"))));

        assertEquals(List.of("A", "B", "C"), registry.all().stream().map(AlarmDataSource::name).toList());
    }

    @Test
    void resolvesByPersistedCode() {
        AlarmDataSource a = dataSource("A");
        AlarmDataSourceRegistry registry = new AlarmDataSourceRegistry(List.of(provider(a)));

        assertEquals(Optional.of(a), registry.find("A"));
        assertEquals(a, registry.get("A"));
    }

    @Test
    void rejectsTwoProvidersClaimingTheSameCode() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new AlarmDataSourceRegistry(
                        List.of(provider(dataSource("A")), provider(dataSource("A")))));

        assertTrue(e.getMessage().contains("A"), e.getMessage());
    }

    @Test
    void rejectsOneProviderClaimingTheSameCodeTwice() {
        assertThrows(IllegalStateException.class,
                () -> new AlarmDataSourceRegistry(
                        List.of(provider(dataSource("A"), dataSource("A")))));
    }

    // A deployable can run without the module that owns a rule's data source, so an
    // unknown code has to be answerable without failing the whole registry.
    @Test
    void findIsEmptyForAnUninstalledCode() {
        AlarmDataSourceRegistry registry = new AlarmDataSourceRegistry(List.of(provider(dataSource("A"))));

        assertFalse(registry.find("B").isPresent());
        assertFalse(registry.find(null).isPresent());
        assertThrows(IllegalArgumentException.class, () -> registry.get("B"));
    }

    @Test
    void hasNoDataSourcesWithoutProviders() {
        assertTrue(new AlarmDataSourceRegistry(List.of()).all().isEmpty());
    }

    @Test
    void realProviderIsResolvable() {
        AlarmDataSourceRegistry registry =
                new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider()));

        assertEquals(TestAlarmDataSource.PRIMARY, registry.get("PRIMARY"));
        assertEquals(TestAlarmDataSource.SECONDARY, registry.get("SECONDARY"));
    }

    private static AlarmDataSourceProvider provider(AlarmDataSource... dataSources) {
        return () -> List.of(dataSources);
    }

    private static AlarmDataSource dataSource(String name) {
        return new AlarmDataSource() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String label() {
                return name;
            }

            @Override
            public List<String> filterKeys() {
                return List.of();
            }

            @Override
            public List<AlarmMetricDefinition> metrics() {
                return List.of();
            }
        };
    }
}
