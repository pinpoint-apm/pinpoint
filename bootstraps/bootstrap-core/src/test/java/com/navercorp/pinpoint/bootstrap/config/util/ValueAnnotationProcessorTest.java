package com.navercorp.pinpoint.bootstrap.config.util;

import com.navercorp.pinpoint.bootstrap.config.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class ValueAnnotationProcessorTest {

    @Test
    public void primitive() {
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

        Assertions.assertEquals(1, config.intValue);
        Assertions.assertEquals(2, config.longValue);
        Assertions.assertEquals(true, config.booleanValue);
        Assertions.assertEquals(3, config.doubleValue, 0);
        Assertions.assertEquals(4, config.flotValue, 0);
        Assertions.assertEquals(5, config.shortValue);
        Assertions.assertEquals(6, config.byteValue);
        Assertions.assertEquals('a', config.charValue);

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

        Assertions.assertEquals(Integer.valueOf(1), config.integerValue);
        Assertions.assertEquals(Long.valueOf(2), config.longValue);
        Assertions.assertEquals("abc", config.stringValue);
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

        Assertions.assertEquals(TYPE.B, config.test);

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

        Assertions.assertEquals("p-A", config.a);
        Assertions.assertEquals("prefix-B", config.b);
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

        Assertions.assertEquals("A", config.a);
    }

    public static class MethodConfig {
        private String a;

        @Value("${a}")
        public void setA(String a) {
            this.a = a;
        }
    }

    public static class DefaultValueConfig {
        @Value("${int:-1}")
        private int intValue;

        @Value("${long:-2}")
        private long longValue;
    }

    @Test
    public void defaultValue() {
        DefaultValueConfig config = new DefaultValueConfig();
        Properties properties = new Properties();
//        properties.put("int", "");
//        properties.put("long", "");


        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(config, properties);

        Assertions.assertEquals(-1, config.intValue);
        Assertions.assertEquals(-2, config.longValue);

    }

}