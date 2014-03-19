package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.vo.Range;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.logging.resources.logging;

import java.io.IOException;

/**
 * @author emeroad
 */
public class ApplicationMapTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void root() throws IOException {
        ApplicationMap app = new ApplicationMap(new Range(0, 1));
        String s = MAPPER.writeValueAsString(app);
        logger.debug(s);

    }
}
