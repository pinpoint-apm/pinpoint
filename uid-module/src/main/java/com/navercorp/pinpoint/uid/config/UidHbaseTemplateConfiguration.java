package com.navercorp.pinpoint.uid.config;

import com.navercorp.pinpoint.common.hbase.ConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseTableFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate;
import com.navercorp.pinpoint.common.hbase.TableFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.hbase.config.ParallelScan;
import com.navercorp.pinpoint.common.hbase.scan.ResultScannerFactory;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricReporter;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.common.util.CpuUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.User;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

@org.springframework.context.annotation.Configuration
public class UidHbaseTemplateConfiguration {

    private final HbaseTemplateConfiguration config = new HbaseTemplateConfiguration();

    @Bean
    @ConfigurationProperties(prefix = "pinpoint.uid.executor")
    public ExecutorProperties uidExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public FactoryBean<ExecutorService> uidHbaseThreadPool(@Qualifier("hbaseExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
                                                           @Qualifier("uidExecutorProperties") ExecutorProperties executorProperties) {
        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, executorProperties);
        factory.setThreadNamePrefix("PinpointUid-" + factory.getThreadNamePrefix());
        return factory;
    }

    @Bean
    public FactoryBean<Connection> uidHbaseConnection(Configuration configuration,
                                                      User user,
                                                      @Qualifier("uidHbaseThreadPool") ExecutorService executorService) {
        return new ConnectionFactoryBean(configuration, user, executorService);
    }

    @Bean
    public TableFactory uidHbaseTableFactory(@Qualifier("uidHbaseConnection") Connection connection) {
        return new HbaseTableFactory(connection);
    }

    @Bean
    public FactoryBean<AsyncConnection> hbaseAsyncPinpointIdConnection(Configuration configuration, User user) {
        return new AsyncConnectionFactoryBean(configuration, user);
    }

    @Bean
    public AsyncTableFactory uidHbaseAsyncTableFactory(@Qualifier("hbaseAsyncPinpointIdConnection") AsyncConnection connection,
                                                       AsyncTableCustomizer customizer) {
        return new HbaseAsyncTableFactory(connection, customizer);
    }

    @Bean
    public HbaseAsyncTemplate uidAsyncTemplate(@Qualifier("uidHbaseAsyncTableFactory") AsyncTableFactory asyncTableFactory,
                                               ScanMetricReporter scanMetricReporter,
                                               ResultScannerFactory resultScannerFactory) {
        ExecutorService executor = newAsyncTemplateExecutor();
        return new HbaseAsyncTemplate(asyncTableFactory, resultScannerFactory, scanMetricReporter, executor);
    }

    private ExecutorService newAsyncTemplateExecutor() {
        ThreadFactory threadFactory = new PinpointThreadFactory("PinpointUid-asyncTemplate", true);
        return ExecutorFactory.newFixedThreadPool(CpuUtils.workerCount(), 1024 * 1024, threadFactory);
    }


    @Bean
    public HbaseTemplate uidHbaseTemplate(@Qualifier("hbaseConfiguration") org.apache.hadoop.conf.Configuration configurable,
                                          @Qualifier("uidHbaseTableFactory") TableFactory tableFactory,
                                          @Qualifier("uidAsyncTemplate") HbaseAsyncTemplate asyncTemplate,
                                          Optional<ParallelScan> parallelScan,
                                          @Value("${hbase.client.nativeAsync:false}") boolean nativeAsync,
                                          ResultScannerFactory resultScannerFactory,
                                          ScanMetricReporter scanMetricReport) {
        return config.hbaseTemplate(configurable, tableFactory, asyncTemplate, parallelScan, nativeAsync, resultScannerFactory, scanMetricReport);
    }

}
