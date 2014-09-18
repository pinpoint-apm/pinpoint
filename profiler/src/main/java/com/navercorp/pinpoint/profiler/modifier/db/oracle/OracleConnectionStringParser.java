package com.nhn.pinpoint.profiler.modifier.db.oracle;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.profiler.modifier.db.StringMaker;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.Description;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.KeyValue;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.OracleConnectionStringException;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.OracleNetConnectionDescriptorParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class OracleConnectionStringParser implements ConnectionStringParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String url) {
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:oracle:").after(":");
        String description = maker.after('@').value().trim();
        if (description.startsWith("(")) {
            return parseNetConnectionUrl(url);
        } else {
            return parseSimpleUrl(url, maker);
        }
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
    private DatabaseInfo parseNetConnectionUrl(String url) {
        try {
            // oracle new URL : rac용
            OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(url);
            KeyValue keyValue = parser.parse();
            // TODO oci 드라이버 일경우의 추가 처리가 필요함. nhn말고 왠간한데는 oci를 더 많이 씀.
//                parser.getDriverType();
            return createOracleDatabaseInfo(keyValue, url);
        } catch (OracleConnectionStringException ex) {
            logger.warn("OracleConnectionString parse error. url:{} Caused:", url, ex.getMessage(), ex);

            // 에러찍고 그냥 unknownDataBase 생성
            return JDBCUrlParser.createUnknownDataBase(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url);
        } catch (Throwable ex) {
            // 나중에 좀더 정교하게 exception을 던지게 되면 OracleConnectionStringException 만 잡는것으로 바꿔야 될듯하다.
            logger.warn("OracleConnectionString parse error. url:{} Caused:", url, ex.getMessage(), ex);
            // 에러찍고 그냥 unknownDataBase 생성
            return JDBCUrlParser.createUnknownDataBase(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url);
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

    private DatabaseInfo createOracleDatabaseInfo(KeyValue keyValue, String url) {

        Description description = new Description(keyValue);
        List<String> jdbcHost = description.getJdbcHost();

        return new DefaultDatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, jdbcHost, description.getDatabaseId());

    }
}
