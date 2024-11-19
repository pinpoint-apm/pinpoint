package com.navercorp.pinpoint.test.plugin.junit5.engine.discovery;

public class TestDescriptorRegistry {

    private static final TestDescriptorBuilder[] builders = new TestDescriptorBuilder[] {
            new PluginTestDescriptorBuilder(),
            new PluginForkedTestDescriptorBuilder(),
            new PluginJunitTestDescriptorBuilder(),
    };

    public TestDescriptorBuilder getDescriptor(Class<?> testClass) {
        for (TestDescriptorBuilder builder : builders) {
            if (builder.test(testClass)) {
                return builder;
            }
        }
        return null;
    }
}
