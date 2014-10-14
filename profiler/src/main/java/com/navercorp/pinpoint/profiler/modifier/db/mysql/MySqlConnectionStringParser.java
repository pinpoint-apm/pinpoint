package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.profiler.modifier.db.StringMaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author emeroad
 */
public class MySqlConnectionStringParser implements ConnectionStringParser {

    // jdbc:mysql:loadbalance://10.25.149.62:3306,10.25.149.61:3306/MySQL?characterEncoding=UTF-8
    private static final String JDBC_MYSQL_LOADBALANCE = "jdbc:mysql:loadbalance:";

    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return JDBCUrlParser.createUnknownDataBase(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, null);
        }

        if (isLoadbalanceUrl(url)) {
            return parseLoadbalancedUrl(url);
        }
        return parseNormal(url);
    }

    private DatabaseInfo parseLoadbalancedUrl(String url) {
        // jdbc:mysql://10.98.133.22:3306/test_lucy_db
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:mysql:");
        // 10.98.133.22:3306 replacation driver같은 경우 n개가 가능할듯.
        // mm db? 의 경우도 고려해야 될듯하다.
        String host = maker.after("//").before('/').value();

        // regex cache코드 삭제. 자주 호출되는 api가 아니라 메모리에 안가지고있는게 좋을듯함하다.
        String[] parsedHost = host.split(",");
        List<String> hostList = Arrays.asList(parsedHost);


        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }

    private boolean isLoadbalanceUrl(String url) {
        return url.regionMatches(true, 0, JDBC_MYSQL_LOADBALANCE, 0, JDBC_MYSQL_LOADBALANCE.length());
    }

    private DatabaseInfo parseNormal(String url) {
        // jdbc:mysql://10.98.133.22:3306/test_lucy_db
        StringMaker maker = new StringMaker(url);
        maker.after("jdbc:mysql:");
        // 10.98.133.22:3306 replacation driver같은 경우 n개가 가능할듯.
        // mm db? 의 경우도 고려해야 될듯하다.
        String host = maker.after("//").before('/').value();
        List<String> hostList = new ArrayList<String>(1);
        hostList.add(host);
        // String port = maker.next().after(':').before('/').value();

        String databaseId = maker.next().afterLast('/').before('?').value();
        String normalizedUrl = maker.clear().before('?').value();
        return new DefaultDatabaseInfo(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }
}
