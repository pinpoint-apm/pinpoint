package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.LinkList;
import com.navercorp.pinpoint.web.applicationmap.NodeList;
import com.navercorp.pinpoint.web.vo.Range;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author emeroad
 */
public class ApplicationMapTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void root() throws IOException {
        ApplicationMap app = new ApplicationMap(new Range(0, 1), new NodeList(), new LinkList());
        String s = MAPPER.writeValueAsString(app);
        logger.debug(s);

    }
}
