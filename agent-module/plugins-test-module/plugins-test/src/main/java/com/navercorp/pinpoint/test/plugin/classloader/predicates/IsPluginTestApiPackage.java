package com.navercorp.pinpoint.test.plugin.classloader.predicates;

/**
 * The annotations and contracts of pinpoint-plugins-test-api that a plugin test class compiles against.
 * Every plugin test class loader must delegate them to the parent so that the engine, the shared instance
 * and the test class see one class identity.
 */
public class IsPluginTestApiPackage extends PackageFilter {
    public static final String[] PACKAGES = new String[] {
            "com.navercorp.pinpoint.test.plugin.api.",
    };

    public IsPluginTestApiPackage() {
        super(PACKAGES);
    }
}
