package com.nhn.pinpoint.profiler.modifier.db;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.jtds.JtdsConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySqlConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OracleConnectionStringParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class JDBCUrlParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<String, DatabaseInfo> cache = new ConcurrentHashMap<String, DatabaseInfo>();

    //http://www.petefreitag.com/articles/jdbc_urls/
    public DatabaseInfo parse(String url) {
        final DatabaseInfo hit = cache.get(url);
        if (hit != null) {
            logger.debug("database url cache hit:{} {}", url, hit);
            return hit;
        }

        final DatabaseInfo databaseInfo = doParse(url);
        final DatabaseInfo old = cache.putIfAbsent(url, databaseInfo);
        if (old != null) {
            return old;
        }
        return databaseInfo;
    }

    private DatabaseInfo doParse(String url) {
        // jdbc 체크
        String lowCaseURL = url.toLowerCase().trim();
        if (!lowCaseURL.startsWith("jdbc:")) {
            return createUnknownDataBase(url);
        }

        if (driverTypeCheck(lowCaseURL, "mysql")) {
            return parseMysql(url);
        }
        if (driverTypeCheck(lowCaseURL, "oracle")) {
            return parseOracle(url);
        }

        if (driverTypeCheck(lowCaseURL, "jtds:sqlserver")) {
            return parseJtds(url);
        }
        if (driverTypeCheck(lowCaseURL, "cubrid")) {
        	return parseCubrid(url);
        }
        return createUnknownDataBase(url);
    }

    private boolean driverTypeCheck(String lowCaseURL, String type) {
        final int jdbcNextIndex = 5;
        return lowCaseURL.startsWith(type, jdbcNextIndex);
    }



    private DatabaseInfo parseOracle(String url) {
        OracleConnectionStringParser parser = new OracleConnectionStringParser();
        return parser.parse(url);

    }

    public static DatabaseInfo createUnknownDataBase(String url) {
        return createUnknownDataBase(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, url);
    }

    public static DatabaseInfo createUnknownDataBase(ServiceType type, ServiceType executeQueryType, String url) {
        List<String> list = new ArrayList<String>();
        list.add("error");
        return new DefaultDatabaseInfo(type, executeQueryType, url, url, list, "error");
    }


    private DatabaseInfo parseMysql(String url) {
        final ConnectionStringParser parser = new MySqlConnectionStringParser();
        return parser.parse(url);
    }

    private DatabaseInfo parseJtds(String url) {
        final JtdsConnectionStringParser parser = new JtdsConnectionStringParser();
        return parser.parse(url);

    }
    

	
	private DatabaseInfo parseCubrid(String url) {
        final ConnectionStringParser parser = new CubridConnectionStringParser();
        return parser.parse(url);
	}
}
