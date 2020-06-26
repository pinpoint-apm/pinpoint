package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DescriptorParserTest {

    @Test
    public void parse() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(OptionKey.getClassName("custom-error-handler"), "java.lang.RuntimeException");
        map.put(OptionKey.getExceptionMessageContains("custom-error-handler"), "error");

        DescriptorParser parser = new DescriptorParser(map);
        List<Descriptor> descriptorList = parser.parse();
        Assert.assertEquals(descriptorList.size(), 1);

        ErrorHandlerBuilder builder = new ErrorHandlerBuilder(descriptorList);
        ErrorHandler errorHandler = builder.build();

        Assert.assertTrue(errorHandler.handleError(new RuntimeException(" error ")));

        Assert.assertFalse(errorHandler.handleError(new RuntimeException(" success")));
        Assert.assertFalse(errorHandler.handleError(new SQLException(" success")));
        Assert.assertFalse(errorHandler.handleError(new SQLException(" error")));
    }
}