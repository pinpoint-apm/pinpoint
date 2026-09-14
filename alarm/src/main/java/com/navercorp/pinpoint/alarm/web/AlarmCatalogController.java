package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/alarm")
public class AlarmCatalogController {

    private final AlarmTemplatePresetLoader templatePresetLoader;
    private final AlarmDataSourceRegistry dataSourceRegistry;

    public AlarmCatalogController(AlarmTemplatePresetLoader templatePresetLoader,
                                  AlarmDataSourceRegistry dataSourceRegistry) {
        this.templatePresetLoader = Objects.requireNonNull(templatePresetLoader, "templatePresetLoader");
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry, "dataSourceRegistry");
    }

    @GetMapping("/datasources")
    public List<AlarmCatalogResponse.DataSource> getDataSources() {
        return dataSourceRegistry.all().stream()
                .map(AlarmCatalogResponse.DataSource::from)
                .toList();
    }

    @GetMapping("/metrics")
    public List<AlarmCatalogResponse.Metric> getMetrics(
            @RequestParam("dataSource") String dataSource) {
        return dataSourceRegistry.get(dataSource).metrics().stream()
                .map(AlarmCatalogResponse.Metric::from)
                .toList();
    }

    @GetMapping("/template-presets")
    public List<AlarmTemplatePreset> getTemplatePresets() {
        return templatePresetLoader.getPresets();
    }
}
