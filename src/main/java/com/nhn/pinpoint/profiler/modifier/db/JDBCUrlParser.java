package com.nhn.pinpoint.profiler.modifier.db;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.Description;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.KeyValue;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.OracleConnectionStringException;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.OracleNetConnectionDescriptorParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public class JDBCUrlParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<String, DefaultDatabaseInfo> cache = new ConcurrentHashMap<String, DefaultDatabaseInfo>();


    public DefaultDatabaseInfo parse(String url) {
        final DefaultDatabaseInfo hit = cache.get(url);
        if (hit != null) {
            logger.debug("database url cache hit:{} {}", url, hit);
            return hit;
        }

        final DefaultDatabaseInfo databaseInfo = doParse(url);
        final DefaultDatabaseInfo old = cache.putIfAbsent(url, databaseInfo);
        if (old != null) {
            return old;
        }
        return databaseInfo;

//        else if (url.indexOf("jdbc:oracle") >= 0) {
//            maker.lower().after("jdbc:oracle:").after(':');
//            info.type = TYPE.ORACLE;
//            String description = maker.after('@').value().trim();
//
//            if (description.startsWith("(")) {
//                Matcher matcher = oracleRAC.matcher(description);
//
//                if (matcher.matches()) {
//                    info.host = matcher.group(1);
//                    info.port = matcher.group(2);
//                    info.databaseId = matcher.group(3);
//                } else {
//                    info.databaseId = "ParsingFailed[" + System.currentTimeMillis() + ']';
//                }
//            } else {
//                info.host = maker.before(':').value();
//                info.port = maker.next().after(':').before(':').value();
//                info.databaseId = maker.next().afterLast(':').value();
//            }
//        } else if (url.indexOf("jdbc:sqlserver") >= 0) {
//            maker.lower().after("jdbc:sqlserver:");
//            info.type = TYPE.MSSQL;
//            info.host = maker.after("//").before(';').value();
//            info.port = maker.currentTraceClear().after("port=").before(';').value();
//            info.databaseId = maker.currentTraceClear().after("databasename=").before(';').value();
//        } else if (url.indexOf("jdbc:jtds:sqlserver") >= 0) {
//            maker.lower().after("jdbc:jtds:sqlserver:");
//            info.type = TYPE.MSSQL;
//            info.host = maker.after("//").beforeLast('/').beforeLast(':').value();
//            info.port = maker.next().after(':').beforeLast('/').value();
//            info.databaseId = maker.next().afterLast('/').before(';').value();
//        } else if (url.indexOf("jdbc:cubrid") >= 0) {
//            maker.lower().after("jdbc:cubrid");
//            info.type = TYPE.CUBRID;
//            info.host = maker.after(':').before(':').value();
//            info.port = maker.next().after(':').before(':').value();
//            info.databaseId = maker.next().after(':').before(':').value();
//        }
//        if ("".equals(info.databaseId)) {
//            info.databaseId = info.host;
//        }

//        return info;
//        return null;
    }

    private DefaultDatabaseInfo doParse(String url) {
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
        if (driverTypeCheck(lowCaseURL, "cubrid")) {
        	return parseCubrid(url);
        }
        return createUnknownDataBase(url);
    }

    private boolean driverTypeCheck(String lowCaseURL, String type) {
        final int jdbcNextIndex = 5;
        return lowCaseURL.startsWith(type, jdbcNextIndex);
    }

    //    rac url.
//    jdbc:oracle:thin:@(Description=(LOAD_BALANCE=on)" +
//    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
//            "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1521))" +
//            "(CONNECT_DATA=(SERVICE_NAME=service)))"
//
//    thin driver url
//    jdbc:oracle:thin:@hostname:port:SID
//    "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
//    들여 쓰기를 통해 token을 보기 좋게 나눈경우.
//    jdbc:oracle:thin:
//    @(
//         Description=(LOAD_BALANCE=on)
//         (
//             ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521)
//         )
//         (
//             ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1521)
//         )
//         (
//             CONNECT_DATA=(SERVICE_NAME=service)
//         )
//    )


    private DefaultDatabaseInfo parseOracle(String url) {
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:oracle:").after(":");
        String description = maker.after('@').value().trim();
        if (description.startsWith("(")) {
            return parseNetConnectionUrl(url);
        } else {
            return parseSimpleUrl(url, maker);
        }
    }

    private DefaultDatabaseInfo parseNetConnectionUrl(String url) {
        try {
            // oracle new URL : rac용
            OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(url);
            KeyValue keyValue = parser.parse();
            // TODO oci 드라이버 일경우의 추가 처리가 필요함. nhn말고 왠간한데는 oci를 더 많이 씀.
//                parser.getDriverType();
            return createOracleDatabaseInfo(keyValue, url);
        } catch (OracleConnectionStringException ex) {
            logger.warn("OracleConnectionString parse error. url:{} Caused:", new Object[]{url, ex.getMessage(), ex});

            // 에러찍고 그냥 unknownDataBase 생성
            return createUnknownDataBase(url);
        } catch (Throwable ex) {
            // 나중에 좀더 정교하게 exception을 던지게 되면 OracleConnectionStringException 만 잡는것으로 바꿔야 될듯하다.
            logger.warn("OracleConnectionString parse error. url:{} Caused:", new Object[]{url, ex.getMessage(), ex});
            // 에러찍고 그냥 unknownDataBase 생성
            return createUnknownDataBase(url);
        }
    }

    private DefaultDatabaseInfo parseSimpleUrl(String url, StringMaker maker) {
        // thin driver
        // jdbc:oracle:thin:@hostname:port:SID
        // "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
//      jdbc:oracle:thin:@//hostname:port/serviceName
        String host = maker.before(':').value();
        String port = maker.next().after(':').before(':', '/').value();
        String databaseId = maker.next().afterLast(':', '/').value();

        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host + ":" + port);
        return new DefaultDatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, hostList, databaseId);
    }

    private DefaultDatabaseInfo createOracleDatabaseInfo(KeyValue keyValue, String url) {

        Description description = new Description(keyValue);
        List<String> jdbcHost = description.getJdbcHost();

        return new DefaultDatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, jdbcHost, description.getDatabaseId());

    }


    private DefaultDatabaseInfo createUnknownDataBase(String url) {
        List<String> list = new ArrayList<String>();
        list.add("error");
        return new DefaultDatabaseInfo(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, url, url, list, "error");
    }


    private DefaultDatabaseInfo parseMysql(String url) {
        //            jdbc:mysql://10.98.133.22:3306/test_lucy_db
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:mysql:");
        // 10.98.133.22:3306 replacation driver같은 경우 n개가 가능할듯.
        // mm db? 의 경우도 고려해야 될듯하다.
        String host = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
//        String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }
    
    /*
	private DatabaseInfo parseCubrid(String url) {
		// jdbc:cubrid:10.101.57.233:30102:pinpoint:::
		StringMaker maker = new StringMaker(url);
		maker.after("jdbc:cubrid:");
		// 10.98.133.22:3306 replacation driver같은 경우 n개가 가능할듯.
		// mm db? 의 경우도 고려해야 될듯하다.
		String host = maker.after("//").before('/').value();
		List<String> hostList = new ArrayList<String>(1);
		hostList.add(host);
		// String port = maker.next().after(':').before('/').value();

		String databaseId = maker.next().afterLast('/').before('?').value();
		String normalizedUrl = maker.clear().before('?').value();
		
		return new DatabaseInfo(ServiceType.CUBRID, ServiceType.CUBRID_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
	}
	*/
	
	private DefaultDatabaseInfo parseCubrid(String url) {
		final String default_hostname = "localhost";
		final int default_port = 30000;
		final String default_user = "public";
		final String default_password = "";

		final String URL_PATTERN = "jdbc:cubrid(-oracle|-mysql)?:([a-zA-Z_0-9\\.-]*):([0-9]*):([^:]+):([^:]*):([^:]*):(\\?[a-zA-Z_0-9]+=[^&=?]+(&[a-zA-Z_0-9]+=[^&=?]+)*)?";

		Pattern pattern = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);

		if (!matcher.find()) {
			return createUnknownDataBase(url);
		}

		String host = matcher.group(2);
		String portString = matcher.group(3);
		String db = matcher.group(4);
		String user = matcher.group(5);
		String pass = matcher.group(6);
		String prop = matcher.group(7);
		int port = default_port;

		String resolvedUrl;

		if (host == null || host.length() == 0) {
			host = default_hostname;
		}

		if (portString == null || portString.length() == 0) {
			port = default_port;
		} else {
			port = Integer.parseInt(portString);
		}

		if (user == null) {
			user = default_user;
		}
		
		if (pass == null) {
			pass = default_password;
		}

		resolvedUrl = "jdbc:cubrid:" + host + ":" + port + ":" + db + ":" + user + ":********:";

		StringMaker maker = new StringMaker(url);
		String normalizedUrl = maker.clear().before('?').value();

		List<String> hostList = new ArrayList<String>(1);
		hostList.add(host);

		// alt host는 제외.

		return new DefaultDatabaseInfo(ServiceType.CUBRID, ServiceType.CUBRID_EXECUTE_QUERY, url, normalizedUrl, hostList, db);
	}
}
