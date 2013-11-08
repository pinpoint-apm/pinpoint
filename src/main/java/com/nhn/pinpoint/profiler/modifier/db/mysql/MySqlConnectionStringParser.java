package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.profiler.modifier.db.StringMaker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class MySqlConnectionStringParser implements ConnectionStringParser {

    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return JDBCUrlParser.createUnknownDataBase(ServiceType.MYSQL, ServiceType.MYSQL_EXECUTE_QUERY, url);
        }
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
