package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.JDBCUrlParser;
import com.nhn.pinpoint.profiler.modifier.db.StringMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public class CubridConnectionStringParser implements ConnectionStringParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 30000;
    public static final String DEFAULT_USER = "public";
    public static final String DEFAULT_PASSWORD = "";

    private static final String URL_PATTERN = "jdbc:cubrid(-oracle|-mysql)?:([a-zA-Z_0-9\\.-]*):([0-9]*):([^:]+):([^:]*):([^:]*):(\\?[a-zA-Z_0-9]+=[^&=?]+(&[a-zA-Z_0-9]+=[^&=?]+)*)?";
    private static final Pattern PATTERN = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);

    @Override
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return JDBCUrlParser.createUnknownDataBase(ServiceType.CUBRID, ServiceType.CUBRID_EXECUTE_QUERY, url);
        }

        final Matcher matcher = PATTERN.matcher(url);
        if (!matcher.find()) {
            logger.warn("Cubrid connectionString parse fail. url:{}", url);
            return JDBCUrlParser.createUnknownDataBase(ServiceType.CUBRID, ServiceType.CUBRID_EXECUTE_QUERY, url);
        }

        String host = matcher.group(2);
        String portString = matcher.group(3);
        String db = matcher.group(4);
        String user = matcher.group(5);
//        String pass = matcher.group(6);
//        String prop = matcher.group(7);

        int port = DEFAULT_PORT;

//        String resolvedUrl;

        if (host == null || host.length() == 0) {
            host = DEFAULT_HOSTNAME;
        }

        if (portString == null || portString.length() == 0) {
            port = DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                logger.warn("cubrid portString parsing fail. portString:{}, url:{}", portString, url);
            }
        }

        if (user == null) {
            user = DEFAULT_USER;
        }

//        if (pass == null) {
//            pass = DEFAULT_PASSWORD;
//        }

//        resolvedUrl = "jdbc:cubrid:" + host + ":" + port + ":" + db + ":" + user + ":********:";

        StringMaker maker = new StringMaker(url);
        String normalizedUrl = maker.clear().before('?').value();

        List<String> hostList = new ArrayList<String>(1);
        final String hostAndPort = host + ":" + portString;
        hostList.add(hostAndPort);

        // alt host는 제외.

        return new DefaultDatabaseInfo(ServiceType.CUBRID, ServiceType.CUBRID_EXECUTE_QUERY, url, normalizedUrl, hostList, db);
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

}
