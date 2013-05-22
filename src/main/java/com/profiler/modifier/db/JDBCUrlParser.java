package com.profiler.modifier.db;

import com.profiler.common.ServiceType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class JDBCUrlParser {



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
//    jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
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
//         DESCRIPTION=(LOAD_BALANCE=on)
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

    static Pattern oracleRAC = Pattern.compile(
            "(" +
                ".*[ADDRESS=].*\\(\\s*HOST\\s*=\\s*([\\w\\.]*\\s*\\)).*\\(\\s*PORT\\s*=\\s*([\\d]*\\s*\\))+" +
            ").*" +
            "\\(\\s*SERVICE_NAME\\s*=\\s*([\\w]*)\\s*\\).*");
//


    private DatabaseInfo parseOracle(String url) {
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:oracle:").after(":");
        String description = maker.after('@').value().trim();

        if (description.startsWith("(")) {
            Matcher matcher = oracleRAC.matcher(description);
            if (matcher.matches()) {
//                n개의 rac 주소를 못찾는 문제가 있음.
//                rac type url의 파서를 추가적으로 개발해야 함.
                String host = matcher.group(1);
                String port = matcher.group(2);
                String databaseId = matcher.group(3);
                for(int i =0; i<matcher.groupCount(); i++ ) {
                    System.out.println(i + ":" + matcher.group(i));
                }

                List<String> hostList = new ArrayList<String>(1);
                hostList.add(host + ":" + port);
                // oracle driver는 option을 connectionString으롤 받지 않기 때문에. normalizedUrl이 없다.
                return new DatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, hostList, databaseId);
            } else {
                // error 처리를 다시 생각해봐야 될듯한다.
                // 그냥 파싱에 실패하면 동작되지 않도록 수정해야 하는게 바람직한가?
                // rac url이 매칭 되지 않았다는 의미인데. rac url 파싱에 실패시 정보를 넣기가 애매함.
                return createUnknownDataBase(url);
            }
        } else {
            // thin driver
            // jdbc:oracle:thin:@hostname:port:SID
            // "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
            String host  = maker.before(':').value();
            String port = maker.next().after(':').before(':').value();
            String databaseId = maker.next().afterLast(':').value();
            List<String> hostList = new ArrayList<String>(1);
            hostList.add(host + ":" + port);
            return new DatabaseInfo(ServiceType.ORACLE, ServiceType.ORACLE_EXECUTE_QUERY, url, url, hostList, databaseId);
        }
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
