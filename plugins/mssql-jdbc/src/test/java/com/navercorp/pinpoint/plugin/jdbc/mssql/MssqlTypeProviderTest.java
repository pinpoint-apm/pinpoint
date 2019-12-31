package com.navercorp.pinpoint.plugin.jdbc.mssql;


import static org.junit.Assert.assertNotNull;

import com.navercorp.pinpoint.common.profiler.trace.ServiceTypeRegistry;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * @author Harris Gwag ( gwagdalf )
 */
public class MssqlTypeProviderTest {

  private static final CommonLoggerFactory LOGGER_FACTORY = StdoutCommonLoggerFactory.INSTANCE;

  @Test
  public void should_load_MssqlServiceType() {
    List<TraceMetadataProvider> typeProviders = Arrays.<TraceMetadataProvider>asList(
        new MssqlTypeProvider());
    TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(LOGGER_FACTORY);
    traceMetadataLoader.load(typeProviders);
    ServiceTypeRegistry serviceTypeRegistry = traceMetadataLoader.createServiceTypeRegistry();

    assertNotNull(serviceTypeRegistry.findServiceType(MssqlConstants.MSSQL_JDBC.getCode()));
    assertNotNull(serviceTypeRegistry.findServiceType(MssqlConstants.MSSQL_JDBC_QUERY.getCode()));

  }


}