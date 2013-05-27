package com.profiler.modifier.db;

import com.profiler.common.ServiceType;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.db.oracle.KeyValue;
import com.profiler.modifier.db.oracle.OracleConnectionStringException;
import com.profiler.modifier.db.oracle.OracleURLParser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JDBCUrlParser {
    private static Logger logger = LoggerFactory.getLogger(JDBCUrlParser.class);


    public DatabaseInfo parse(String url) {
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
        return createUnknownDataBase(url);
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


    private DatabaseInfo parseOracle(String url) {
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:oracle:").after(":");
        String description = maker.after('@').value().trim();

        if (description.startsWith("(")) {
            try {
                // oracle new URL : rac용
                OracleURLParser parser = new OracleURLParser(url);
                KeyValue keyValue = parser.parse();

                return createOracleDatabaseInfo(keyValue, url);
            } catch (OracleConnectionStringException ex) {
                logger.warn("OracleConnectionStringParse Error Caused:", ex.getMessage(), ex);
                logger.warn("OracleConnectionString parse error:{}", url);
                // 에러찍고 그냥 unknownDataBase 생성
            }
            return createUnknownDataBase(url);
        } else {
            // thin driver
            // jdbc:oracle:thin:@hostname:port:SID
            // "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
            String host = maker.before(':').value();
            String port = maker.next().after(':').before(':').value();
            String databaseId = maker.next().afterLast(':').value();
            List<String> hostList = new ArrayList<String>(1);
            hostList.add(host + ":" + port);
            return new DatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, hostList, databaseId);
        }
    }

    private DatabaseInfo createOracleDatabaseInfo(KeyValue keyValue, String url) {
        if (!"description".equals(keyValue.getKey())) {
            throw new OracleConnectionStringException("description not exist");
        }

        List<String> hostList = findAddress(keyValue);
        String oracleDatabaseId = getOracleDatabaseId(keyValue);
        return new DatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, hostList, oracleDatabaseId);

    }

    public String getOracleDatabaseId(KeyValue keyValue) {
        List<KeyValue> keyValueList = keyValue.getKeyValueList();
        for (KeyValue kv : keyValueList) {
            if ("connect_data".equals(kv.getKey())) {
                List<KeyValue> connectDataList = kv.getKeyValueList();
                for (KeyValue connectDataNode : connectDataList) {
                    if ("service_name".equals(connectDataNode.getKey())) {
                        return connectDataNode.getValue();
                    }
                }
            }
        }
        return "oracleDatabaseId not found";
    }

    private List<String> findAddress(KeyValue keyValue) {
        List<String> hostList = new ArrayList<String>();

        List<KeyValue> keyValueList = keyValue.getKeyValueList();
        for (KeyValue kv : keyValueList) {
            if ("address".equals(kv.getKey())) {
                KeyValue host = new KeyValue();
                for (KeyValue addressChild : kv.getKeyValueList()) {
                    if ("host".equals(addressChild.getKey())) {
                        host.setKey(addressChild.getValue());
                    } else if ("port".equals(addressChild.getKey())) {
                        host.setValue(addressChild.getValue());
                    }
                }
                hostList.add(host.getKey() + ":" + host.getValue());
            }
        }
        return hostList;
    }

    private DatabaseInfo createUnknownDataBase(String url) {
        List<String> list = new ArrayList<String>();
        list.add("error");
        return new DatabaseInfo(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, url, url, list, "error");
    }


    private DatabaseInfo parseMysql(String url) {
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
        return new DatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }


}
