package com.nhn.pinpoint.profiler.modifier.db.jtds;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.profiler.modifier.db.StringMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class JtdsConnectionStringParser implements ConnectionStringParser {

    public static final int DEFAULT_PORT = 1433;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return JDBCUrlParser.createUnknownDataBase(ServiceType.MSSQL, ServiceType.MSSQL_EXECUTE_QUERY, null);
        }

//        jdbc:jtds:sqlserver://10.xx.xx.xx:1433;DatabaseName=CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        StringMaker maker = new StringMaker(url);

        maker.lower().after("jdbc:jtds:sqlserver:");

        StringMaker before = maker.after("//").before(';');
        final String hostAndPortAndDataBaseString = before.value();
        String databaseId = "";
        String hostAndPortString = "";
        final int databaseIdIndex = hostAndPortAndDataBaseString.indexOf('/');
        if (databaseIdIndex != -1) {
            hostAndPortString = hostAndPortAndDataBaseString.substring(0, databaseIdIndex);
            databaseId = hostAndPortAndDataBaseString.substring(databaseIdIndex+1, hostAndPortAndDataBaseString.length());
        } else {
            hostAndPortString = hostAndPortAndDataBaseString;
        }

        List<String> hostList = new ArrayList<String>(1);
        hostList.add(hostAndPortString);
        // option properties search
        if (databaseId.isEmpty()) {
            databaseId = maker.next().after("databasename=").before(';').value();
        }

        String normalizedUrl = maker.clear().before(";").value();

        return new DefaultDatabaseInfo(ServiceType.MSSQL, ServiceType.MSSQL_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }


}
