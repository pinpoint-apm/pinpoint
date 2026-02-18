
package com.navercorp.pinpoint.plugin.db2;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class DB2JdbcUrlParser implements JdbcUrlParserV2 {

	
    static final String DB2_URL_PREFIX = "jdbc:db2:";
    private static final Set<Type> TYPES = EnumSet.allOf(Type.class);

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl");
            return UnKnownDatabaseInfo.INSTANCE;
        }

        Type type = getType(jdbcUrl);
        if (type == null) {
            logger.info("jdbcUrl has invalid prefix.(url:{}, valid prefixes:{}, {})", jdbcUrl, DB2_URL_PREFIX, DB2_URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        try {
            return parse0(jdbcUrl, type);
        } catch (Exception e) {
            logger.info("DB2JdbcUrl parse error. url: {}, Caused: {}", jdbcUrl, e.getMessage(), e);
            return UnKnownDatabaseInfo.createUnknownDataBase(DB2PluginConstants.DB2,DB2PluginConstants.DB2_EXECUTE_QUERY, jdbcUrl);
        }
    }

    private DatabaseInfo parse0(String url, Type type) {
        return parseNormal(url, type);
    }

    private DatabaseInfo parseNormal(String url, Type type) {
       
        StringMaker maker = new StringMaker(url);
        maker.after(type.getUrlPrefix());
        
        String host = maker.after("//").before('/').value();
        List<String> hostList = parseHost(host);

        String databaseId = maker.next().after('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(DB2PluginConstants.DB2, DB2PluginConstants.DB2_EXECUTE_QUERY, url,
                normalizedUrl, hostList, databaseId);
    }

    private List<String> parseHost(String host) {
        final int multiHost = host.indexOf(",");
        if (multiHost == -1) {
            return Collections.singletonList(host);
        }
       
        String[] parsedHost = host.split(",");
        return Arrays.asList(parsedHost);
    }

    @Override
    public ServiceType getServiceType() {
        return DB2PluginConstants.DB2;
    }

    private static Type getType(String jdbcUrl) {
        for (Type type : TYPES) {
            if (jdbcUrl.startsWith(type.getUrlPrefix())) {
                return type;
            }
        }
        return null;
    }

    private enum Type {
        DB2(DB2_URL_PREFIX);

        private final String urlPrefix;
        private final String loadbalanceUrlPrefix;

        Type(String urlPrefix) {
            this.urlPrefix = urlPrefix;
            this.loadbalanceUrlPrefix = urlPrefix + "loadbalance:";
        }

        private String getUrlPrefix() {
            return urlPrefix;
        }

        private String getLoadbalanceUrlPrefix() {
            return loadbalanceUrlPrefix;
        }
    }
}
