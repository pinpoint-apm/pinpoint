package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Header;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Hashtable;


public class HeaderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testToString() throws Exception {
        logger.debug("{}", Header.HTTP_FLAGS);
    }


	@Test
	public void isHeaderKey() throws Exception {
		Assert.assertTrue(Header.hasHeader(Header.HTTP_FLAGS.toString()));

		Assert.assertFalse(Header.hasHeader("Not_Exist"));

		Assert.assertFalse(Header.hasHeader(null));
	}


	@Test
	public void getHeaders() {
		Enumeration enumeration = Header.getHeaders(Header.HTTP_FLAGS.toString());

		Assert.assertFalse(enumeration.hasMoreElements());
		Assert.assertNull(enumeration.nextElement());

		Enumeration needNull = Header.getHeaders("test");
		Assert.assertNull(needNull);

	}

	@Test
	public void filteredHeaderNames() throws Exception {
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put("a", "aa");
		hashtable.put("b", Header.HTTP_FLAGS.toString());
		hashtable.put("c", "cc");
		Enumeration<String> elements = hashtable.elements();

		Enumeration enumeration = Header.filteredHeaderNames(elements);
		int count = 0;
		while(enumeration.hasMoreElements()) {
			count++;
			Assert.assertFalse(Header.hasHeader((String) enumeration.nextElement()));
		}
		Assert.assertEquals(count, 2);

	}


}
