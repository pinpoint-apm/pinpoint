/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.starter.multi.application.type;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.NonInteractiveShellRunner;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author youngjin.kim2
 */
@Configuration
public class ShellCollectorTypeParser implements CollectorTypeParser {

    private static final ServerBootLogger logger = ServerBootLogger.getLogger(ShellCollectorTypeParser.class);

    @Override
    public CollectorTypeSet parse(String[] args) {
        try {
            ApplicationContext ctx = initApplicationContext();
            ctx.getBean(NonInteractiveShellRunner.class).run(wrapArgs(args));
            List<CollectorType> types = ctx.getBean("collectorTypeSink", CollectorTypeSink.class).get();
            return new CollectorTypeSet(Set.copyOf(types));
        } catch (Exception e) {
            logger.error("Failed to parse collector types", e);
            return new CollectorTypeSet(Set.of());
        }
    }

    private static ApplicationContext initApplicationContext() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ShellCollectorTypeParser.class);
        ctx.scan("org.springframework.shell.boot");
        ctx.refresh();
        return ctx;
    }

    private static ApplicationArguments wrapArgs(String[] args) {
        if (args == null || args.length == 0) {
            return new DefaultApplicationArguments("run");
        }
        return new DefaultApplicationArguments(args);
    }

    @Bean("collectorTypeSink")
    public CollectorTypeSink collectorTypesOutput() {
        return new CollectorTypeSink();
    }

    @ShellComponent
    public static class CollectorRunnerShellComponent {

        private final CollectorTypeSink collectorTypeSink;

        public CollectorRunnerShellComponent(@Qualifier("collectorTypeSink") CollectorTypeSink collectorTypeSink) {
            this.collectorTypeSink = Objects.requireNonNull(collectorTypeSink, "collectorTypeSink");
        }

        @ShellMethod(key = "run", value = "Run pinpoint collector server")
        @SuppressWarnings("unused") // False positive
        public void run(
                @ShellOption(
                        help = "comma separated types of pinpoint collector (BASIC | BASIC_WITH_INSPECTOR | | METRIC | LOG | ALL)",
                        defaultValue = "ALL"
                )
                List<String> types
        ) {
            types.stream()
                    .map(String::toUpperCase)
                    .map(CollectorType::valueOf)
                    .forEach(collectorTypeSink::add);
        }

    }

    public static class CollectorTypeSink {

        private final List<CollectorType> types = new CopyOnWriteArrayList<>();

        public void add(CollectorType type) {
            types.add(type);
        }

        public List<CollectorType> get() {
            return new ArrayList<>(types);
        }

    }

}
