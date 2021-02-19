package com.navercorp.pinpoint.bootstrap.config.util;

import com.navercorp.pinpoint.bootstrap.config.Value;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class ValueAnnotationProcessorTest {

    @Test
    public void primitive() throws Exception {
        PrimitiveConfig config = new PrimitiveConfig();
        Properties properties = new Properties();

        properties.setProperty("int", "1");
        properties.setProperty("long", "2");
        properties.setProperty("boolean", "true");
        properties.setProperty("double", "3");
        properties.setProperty("float", "4");
        properties.setProperty("short", "5");
        properties.setProperty("byte", "6");
        properties.setProperty("char", "a");

        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(config, properties);

        Assert.assertEquals(1, config.intValue);
        Assert.assertEquals(2, config.longValue);
        Assert.assertEquals(true, config.booleanValue);
        Assert.assertEquals(3, config.doubleValue, 0);
        Assert.assertEquals(4, config.flotValue, 0);
        Assert.assertEquals(5, config.shortValue);
        Assert.assertEquals(6, config.byteValue);
        Assert.assertEquals('a', config.charValue);

    }

    public static class PrimitiveConfig {
        @Value("${int}")
        private int intValue;

        @Value("${long}")
        private long longValue;

        @Value("${boolean}")
        private boolean booleanValue;

        @Value("${double}")
        private double doubleValue;

        @Value("${float}")
        private float flotValue;

        @Value("${short}")
        private short shortValue;

        @Value("${byte}")
        private byte byteValue;

        @Value("${char}")
        private char charValue;

    }

    @Test
    public void object() {
        Config config = new Config();
        Properties properties = new Properties();

        properties.setProperty("integer", "1");
        properties.setProperty("long", "2");
        properties.setProperty("string", "abc");


        ValueAnnotationProcessor processor = new ValueAnnotationProcessor();
        processor.process(config, properties);

        Assert.assertEquals(Integer.valueOf(1), config.integerValue);
        Assert.assertEquals(Long.valueOf(2), config.longValue);
        Assert.assertEquals("abc", config.stringValue);
    }

    public static class Config {
        @Value("${integer}")
        private Integer integerValue;

        @Value("${long}")
        private Long longValue;

        @Value("${string}")
        private String stringValue;

    }

    enum TYPE {
        A, B;
    }

    public static class EnumConfig {
        @Value("${type.enum}")
        public TYPE test;
    }

    @Test
    public void test_enum() {
        EnumConfig config = new EnumConfig();
        Properties properties = new Properties();

        properties.setProperty("type.enum", "B");

        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(config, properties);

        Assert.assertEquals(TYPE.B, config.test);

    }


    @Test
    public void prefix() {
        PrefixConfig config = new PrefixConfig();

        Properties properties = new Properties();

        properties.setProperty("prefix", "p");
        properties.setProperty("a", "A");
        properties.setProperty("b", "B");

        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(config, properties);

        Assert.assertEquals("p-A", config.a);
        Assert.assertEquals("prefix-B", config.b);
    }

    public static class PrefixConfig {
        @Value("${prefix}-${a}")
        private String a;
        @Value("prefix-${b}")
        private String b;

    }

    @Test
    public void method() {
        MethodConfig config = new MethodConfig();

        Properties properties = new Properties();

        properties.setProperty("a", "A");

        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(config, properties);

        Assert.assertEquals("A", config.a);
    }

    public static class MethodConfig {
        private String a;

        @Value("${a}")
        public void setA(String a) {
            this.a = a;
        }
    }

}