package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.async.AsyncBufferedMutatorCustomizer;
import com.navercorp.pinpoint.common.hbase.async.AsyncBufferedMutatorFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.async.AsyncHbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.AsyncPollerOption;
import com.navercorp.pinpoint.common.hbase.async.AsyncPollingPutWriter;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableWriterFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableWriterSelectorFactory;
import com.navercorp.pinpoint.common.hbase.async.BatchAsyncHbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.ConcurrencyDecorator;
import com.navercorp.pinpoint.common.hbase.async.ConnectionSelector;
import com.navercorp.pinpoint.common.hbase.async.DefaultAsyncBufferedMutatorCustomizer;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncBufferedMutatorFactory;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriterDecorator;
import com.navercorp.pinpoint.common.hbase.async.LoggingHbasePutWriter;
import com.navercorp.pinpoint.common.hbase.async.RoundRobinSelector;
import com.navercorp.pinpoint.common.hbase.async.SimpleConnectionSelector;
import com.navercorp.pinpoint.common.hbase.async.TableWriterFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.security.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@org.springframework.context.annotation.Configuration
public class HbasePutWriterConfiguration {

    @org.springframework.context.annotation.Configuration
    @ConditionalOnProperty(name = "hbase.client.put-writer", havingValue = "asyncTable")
    public static class AsyncHbasePutWriterConfig {
        private final Logger logger = LogManager.getLogger(AsyncHbasePutWriterConfig.class);

        public AsyncHbasePutWriterConfig() {
            logger.info("Install {}", AsyncHbasePutWriterConfig.class.getSimpleName());
        }

        @Primary
        @Bean
        public HbasePutWriter hbasePutWriter(@Qualifier("hbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory,
                                             @Qualifier("concurrencyDecorator") HbasePutWriterDecorator decorator) {
            HbasePutWriter putWriter = newPutWriter(asyncTableFactory, decorator);
            logger.info("hbasePutWriter {}", putWriter);
            return putWriter;
        }

        @Bean
        public HbasePutWriterDecorator concurrencyDecorator(@Value("${hbase.client.put-writer.concurrency-limit:100000}") int concurrency) {
            return new ConcurrencyDecorator(concurrency);
        }

        @Bean
        public HbasePutWriter spanPutWriter(@Qualifier("hbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory,
                                            @Qualifier("spanConcurrencyDecorator") HbasePutWriterDecorator decorator) {
            HbasePutWriter putWriter = newPutWriter(asyncTableFactory, decorator);
            logger.info("hbaseSpanPutWriter {}", putWriter);
            return putWriter;
        }

        @Bean
        public HbasePutWriterDecorator spanConcurrencyDecorator(@Value("${hbase.client.span-put-writer.concurrency-limit:1000000}") int concurrency) {
            return new ConcurrencyDecorator(concurrency);
        }

        private HbasePutWriter newPutWriter(AsyncTableFactory asyncTableFactory, HbasePutWriterDecorator decorator) {
            HbasePutWriter writer = new AsyncHbasePutWriter(asyncTableFactory);
            HbasePutWriter putWriter = decorator.decorator(writer);
            return new LoggingHbasePutWriter(putWriter);
        }
    }


    @org.springframework.context.annotation.Configuration
    @ConditionalOnProperty(name = "hbase.client.put-writer", havingValue = "asyncBufferedMutator", matchIfMissing = true)
    public static class AsyncBufferedHbasePutWriterConfig {
        private final Logger logger = LogManager.getLogger(AsyncBufferedHbasePutWriterConfig.class);

        public AsyncBufferedHbasePutWriterConfig() {
            logger.info("Install {}", AsyncBufferedHbasePutWriterConfig.class.getSimpleName());
        }

        @Bean
        @ConfigurationProperties(prefix = "hbase.client.put-writer.async-buffered-mutator")
        public AsyncBufferedMutatorCustomizer asyncBufferedMutatorCustomizer() {
            return new DefaultAsyncBufferedMutatorCustomizer();
        }

        @Bean
        public AsyncBufferedMutatorFactory hbaseAsyncBufferedMutatorFactory(@Qualifier("hbaseAsyncConnection")
                                                                            AsyncConnection connection,
                                                                            AsyncBufferedMutatorCustomizer customizer) {
            logger.info("AsyncBufferedMutatorCustomizer {}", customizer);
            return new HbaseAsyncBufferedMutatorFactory(connection, customizer);
        }

        @Primary
        @Bean
        public HbasePutWriter hbasePutWriter(@Qualifier("hbaseAsyncBufferedMutatorFactory") AsyncBufferedMutatorFactory asyncTableFactory,
                                             @Qualifier("concurrencyDecorator") HbasePutWriterDecorator decorator) {
            HbasePutWriter hbasePutWriter = newPutWriter(asyncTableFactory, decorator);
            logger.info("hbasePutWriter {}", hbasePutWriter);
            return hbasePutWriter;
        }

        @Bean
        public HbasePutWriterDecorator concurrencyDecorator(@Value("${hbase.client.put-writer.concurrency-limit:100000}") int concurrency) {
            return new ConcurrencyDecorator(concurrency);
        }

        @Bean
        public HbasePutWriter spanPutWriter(@Qualifier("hbaseAsyncBufferedMutatorFactory") AsyncBufferedMutatorFactory asyncTableFactory,
                                            @Qualifier("spanConcurrencyDecorator") HbasePutWriterDecorator decorator) {
            HbasePutWriter hbasePutWriter = newPutWriter(asyncTableFactory, decorator);
            logger.info("HbaseSpanPutWriter {}", hbasePutWriter);
            return hbasePutWriter;
        }

        @Bean
        public HbasePutWriterDecorator spanConcurrencyDecorator(@Value("${hbase.client.span-put-writer.concurrency-limit:1000000}") int concurrency) {
            return new ConcurrencyDecorator(concurrency);
        }

        private HbasePutWriter newPutWriter(AsyncBufferedMutatorFactory asyncTableFactory, HbasePutWriterDecorator decorator) {
            HbasePutWriter writer = new BatchAsyncHbasePutWriter(asyncTableFactory);
            HbasePutWriter putWriter = decorator.decorator(writer);
            return new LoggingHbasePutWriter(putWriter);
        }
    }

    @org.springframework.context.annotation.Configuration
    @ConditionalOnProperty(name = "hbase.client.put-writer", havingValue = "asyncPoller")
    public static class AsyncPollerPutWriterConfig {
        private final Logger logger = LogManager.getLogger(AsyncPollerPutWriterConfig.class);

        public AsyncPollerPutWriterConfig() {
            logger.info("Install {}", AsyncPollerPutWriterConfig.class.getSimpleName());
        }

        @ConfigurationProperties(prefix = "hbase.client.put-writer.async-poller.span")
        @Bean
        public AsyncPollerOption spanPollerOption() {
            return new AsyncPollerOption();
        }

        @Primary
        @Bean
        public HbasePutWriter hbasePutWriter(@Qualifier("hbaseAsyncConnection") AsyncConnection connection,
                                             @Qualifier("concurrencyDecorator") HbasePutWriterDecorator decorator,
                                             @Qualifier("defaultPollerOption")
                                             AsyncPollerOption option) {

            TableWriterFactory factory = new AsyncTableWriterFactory(connection);
            HbasePutWriter hbasePutWriter = newPollerWriter("hbaseAsyncPoller-", factory, decorator, option);
            logger.info("HbasePollerPutWriter {}", hbasePutWriter);
            return hbasePutWriter;
        }

        @ConfigurationProperties(prefix = "hbase.client.put-writer.async-poller.default")
        @Bean
        public AsyncPollerOption defaultPollerOption() {
            return new AsyncPollerOption();
        }

        @Bean
        public HbasePutWriterDecorator concurrencyDecorator(@Value("${hbase.client.put-writer.concurrency-limit:100000}") int concurrency) {
            return new ConcurrencyDecorator(concurrency);
        }

        @Bean
        public ConnectionSelector spanAsyncConnection(Configuration configuration,
                                                      User user,
                                                      @Qualifier("hbaseAsyncConnectionWarmup")
                                                      Optional<Consumer<AsyncConnection>> warmup,
                                                      @Qualifier("spanPollerOption")
                                                      AsyncPollerOption option) throws Exception {
            final int connectionSize = option.getConnectionSize();
            logger.info("ConnectionSelector connectionSize:{}", connectionSize);
            if (connectionSize == 1) {
                AsyncConnectionFactoryBean factory = newAsyncConnectionFactoryBean(configuration, user, warmup);
                return new SimpleConnectionSelector(factory.getObject());
            }
            List<AsyncConnection> connections = new ArrayList<>();
            for (int i = 0; i < connectionSize; i++) {
                AsyncConnectionFactoryBean factory = newAsyncConnectionFactoryBean(configuration, user, warmup);

                connections.add(factory.getObject());
            }
            return new RoundRobinSelector(connections);
        }

        private AsyncConnectionFactoryBean newAsyncConnectionFactoryBean(Configuration configuration,
                                                                         User user,
                                                                         Optional<Consumer<AsyncConnection>> warmup) throws Exception {
            AsyncConnectionFactoryBean factory = new AsyncConnectionFactoryBean(configuration, user);
            warmup.ifPresent(factory::setPostProcessor);
            factory.afterPropertiesSet();
            return factory;
        }

        @Bean
        public HbasePutWriter spanPutWriter(@Qualifier("spanAsyncConnection") ConnectionSelector connection,
                                            @Qualifier("spanConcurrencyDecorator") HbasePutWriterDecorator decorator,
                                            @Qualifier("defaultPollerOption")
                                            AsyncPollerOption option) {

            TableWriterFactory factory = new AsyncTableWriterSelectorFactory(connection);
            HbasePutWriter hbasePutWriter = newPollerWriter("spanAsyncPoller-", factory, decorator, option);
            logger.info("SpanPollerPutWriter {}", hbasePutWriter);
            return hbasePutWriter;
        }

        @Bean
        public HbasePutWriterDecorator spanConcurrencyDecorator(@Value("${hbase.client.span-put-writer.concurrency-limit:1000000}") int concurrency) {
            return new ConcurrencyDecorator(concurrency);
        }

        private HbasePutWriter newPollerWriter(String name,
                                               TableWriterFactory factory,
                                               HbasePutWriterDecorator decorator,
                                               AsyncPollerOption option) {
            HbasePutWriter writer = new AsyncPollingPutWriter(name, factory, option);
            HbasePutWriter putWriter = decorator.decorator(writer);
            return new LoggingHbasePutWriter(putWriter);
        }
    }

}
