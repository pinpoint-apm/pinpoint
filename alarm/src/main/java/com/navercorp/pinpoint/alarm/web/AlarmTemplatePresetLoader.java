package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.validation.ConditionValidator;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Loads the built-in alarm template presets from the classpath — one JSON file
 * per preset under {@value #PRESET_LOCATION_PATTERN}, listed in filename order —
 * and validates every rule at startup, so a broken preset fails deployment
 * instead of surfacing when a user opens the template start view.
 */
@Component
public class AlarmTemplatePresetLoader {

    private static final Logger logger = LogManager.getLogger(AlarmTemplatePresetLoader.class);

    private static final String PRESET_LOCATION_PATTERN = "classpath*:alarm/presets/*.json";

    private final List<AlarmTemplatePreset> presets;
    private final AlarmDataSourceRegistry dataSourceRegistry;

    @Autowired
    public AlarmTemplatePresetLoader(ObjectMapper objectMapper,
                                     ConditionValidator conditionValidator,
                                     FilterKeyValidator filterKeyValidator,
                                     AlarmDataSourceRegistry dataSourceRegistry) {
        this(objectMapper, conditionValidator, filterKeyValidator, dataSourceRegistry,
                resolvePresetResources());
    }

    AlarmTemplatePresetLoader(ObjectMapper objectMapper,
                              ConditionValidator conditionValidator,
                              FilterKeyValidator filterKeyValidator,
                              AlarmDataSourceRegistry dataSourceRegistry,
                              List<Resource> resources) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        Objects.requireNonNull(conditionValidator, "conditionValidator");
        Objects.requireNonNull(filterKeyValidator, "filterKeyValidator");
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry, "dataSourceRegistry");

        // Presets are content a distribution supplies, not something this module ships,
        // so a deployment that offers none still starts -- it simply offers no preset. It is
        // logged because the other way to end up here is a catalog that moved or stopped
        // being packaged, which otherwise looks the same from the outside: an empty rule
        // editor and nothing said about why.
        if (resources.isEmpty()) {
            logger.warn("No alarm template presets found on the classpath ({}); "
                    + "the rule editor will offer none", PRESET_LOCATION_PATTERN);
        }
        List<AlarmTemplatePreset> presets = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            presets.add(read(objectMapper, resource));
        }
        validate(presets, conditionValidator, filterKeyValidator, dataSourceRegistry);
        this.presets = List.copyOf(presets);
    }

    public List<AlarmTemplatePreset> getPresets() {
        return presets;
    }

    private static List<Resource> resolvePresetResources() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(PRESET_LOCATION_PATTERN);
            return Arrays.stream(resources)
                    .sorted(Comparator.comparing(Resource::getFilename,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to resolve alarm template presets: " + PRESET_LOCATION_PATTERN, e);
        }
    }

    private static AlarmTemplatePreset read(ObjectMapper objectMapper, Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, AlarmTemplatePreset.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read alarm template preset: " + resource, e);
        }
    }

    private static void validate(List<AlarmTemplatePreset> presets,
                                 ConditionValidator conditionValidator,
                                 FilterKeyValidator filterKeyValidator,
                                 AlarmDataSourceRegistry dataSourceRegistry) {
        // Per locale: a preset whose Korean and English names are identical (common
        // for English product terms) must not collide with itself.
        Set<String> koNames = new HashSet<>();
        Set<String> enNames = new HashSet<>();
        for (AlarmTemplatePreset preset : presets) {
            validateLocalizedText(preset.name(), "preset name");
            // Optional, like a rule's, but a half-translated one shows a blank in one locale.
            if (preset.description() != null) {
                validateLocalizedText(preset.description(), "preset description");
            }
            boolean koAdded = koNames.add(preset.name().ko());
            boolean enAdded = enNames.add(preset.name().en());
            if (!koAdded || !enAdded) {
                throw new IllegalStateException("Duplicate preset name: " + preset.name().ko());
            }
            if (CollectionUtils.isEmpty(preset.rules())) {
                throw new IllegalStateException("Preset '" + preset.name().ko() + "' must have at least one rule");
            }
            for (AlarmTemplatePreset.Rule rule : preset.rules()) {
                validateRule(preset.name().ko(), rule, conditionValidator, filterKeyValidator,
                        dataSourceRegistry);
            }
        }
    }

    private static void validateRule(String presetName,
                                     AlarmTemplatePreset.Rule rule,
                                     ConditionValidator conditionValidator,
                                     FilterKeyValidator filterKeyValidator,
                                     AlarmDataSourceRegistry dataSourceRegistry) {
        try {
            validateLocalizedText(rule.name(), "rule name");
            // Optional, but a half-translated one would show a blank in one locale.
            if (rule.description() != null) {
                validateLocalizedText(rule.description(), "rule description");
            }
            if (rule.severity() == null) {
                throw new IllegalArgumentException("rule must have a severity");
            }
            if (rule.dataSource() == null) {
                throw new IllegalArgumentException("rule must have a dataSource");
            }
            // Presets prefill the template form as-is, so they must hold exact
            // picker values rather than relying on save-time interval round-up.
            if (!AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS.contains(rule.checkIntervalSec())) {
                throw new IllegalArgumentException("invalid checkIntervalSec: " + rule.checkIntervalSec());
            }
            if (!AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS.contains(rule.actionIntervalSec())) {
                throw new IllegalArgumentException("invalid actionIntervalSec: " + rule.actionIntervalSec());
            }
            conditionValidator.validate(rule.conditions());
            AlarmDataSource dataSource = dataSourceRegistry.get(rule.dataSource());
            filterKeyValidator.validateConditions(dataSource, rule.conditions());
            filterKeyValidator.validateFilters(dataSource, rule.filters());
        } catch (RuntimeException e) {
            String ruleName = rule.name() == null ? null : rule.name().ko();
            throw new IllegalStateException(
                    "Invalid preset rule '" + ruleName + "' in preset '" + presetName + "': " + e.getMessage(), e);
        }
    }

    private static void validateLocalizedText(AlarmTemplatePreset.LocalizedText text, String field) {
        if (text == null || !StringUtils.hasText(text.ko()) || !StringUtils.hasText(text.en())) {
            throw new IllegalStateException(field + " must have both ko and en texts");
        }
    }
}
