package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.api.ClassLoading;
import com.navercorp.pinpoint.test.plugin.api.ClassLoding;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.OnClassLoader;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycle;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigResolverTest {

    private final ConfigResolver resolver = new ConfigResolver();

    @Test
    public void getClassLoading_defaultsToChild() {
        Assertions.assertEquals(ClassLoading.Child, resolver.getClassLoading(null));
        Assertions.assertEquals(ClassLoading.Child, resolver.getClassLoading(annotation(DefaultType.class)));
    }

    @Test
    public void getClassLoading_explicitValue() {
        Assertions.assertEquals(ClassLoading.System, resolver.getClassLoading(annotation(SystemValue.class)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void getClassLoading_deprecatedTypeStillHonored() {
        Assertions.assertEquals(ClassLoading.System, resolver.getClassLoading(annotation(SystemLegacyType.class)));
    }

    @Test
    public void getDependency_nullWhenAbsent() {
        Assertions.assertNull(resolver.getDependency(null));
        Assertions.assertArrayEquals(new String[]{"g:a:1"}, resolver.getDependency(Annotated.class.getAnnotation(Dependency.class)));
    }

    @Test
    public void getSharedDependency_emptyWhenAbsent() {
        Assertions.assertArrayEquals(new String[0], resolver.getSharedDependency(null));
        Assertions.assertArrayEquals(new String[]{"g:s:1"}, resolver.getSharedDependency(Annotated.class.getAnnotation(SharedDependency.class)));
    }

    @Test
    public void getSharedTestLifeCycleClass_nullWhenAbsent() {
        Assertions.assertNull(resolver.getSharedTestLifeCycleClass(null));
        Assertions.assertEquals(LifeCycle.class, resolver.getSharedTestLifeCycleClass(Annotated.class.getAnnotation(SharedTestLifeCycleClass.class)));
    }

    private static OnClassLoader annotation(Class<?> clazz) {
        return clazz.getAnnotation(OnClassLoader.class);
    }

    @OnClassLoader
    private static class DefaultType {
    }

    @OnClassLoader(ClassLoading.System)
    private static class SystemValue {
    }

    @SuppressWarnings("deprecation")
    @OnClassLoader(type = ClassLoding.System)
    private static class SystemLegacyType {
    }

    @Dependency("g:a:1")
    @SharedDependency("g:s:1")
    @SharedTestLifeCycleClass(LifeCycle.class)
    private static class Annotated {
    }

    public static class LifeCycle implements SharedTestLifeCycle {
        @Override
        public java.util.Properties beforeAll() {
            return new java.util.Properties();
        }

        @Override
        public void afterAll() {
        }
    }
}
