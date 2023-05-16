package com.navercorp.pinpoint.plugin.hbase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HbaseVersionTest {

    @Test
    public void getVersion() {
        ClassLoader testCL = this.getClass().getClassLoader();
        int version = HbaseVersion.getVersion(testCL);
        Assertions.assertEquals(HbaseVersion.HBASE_VERSION_1, version);
    }

    @Test
    public void getVersion_hbase1() {
        ClassLoader testCL = this.getClass().getClassLoader();
        ClassLoader childCL = new ClassLoader(testCL) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (HbaseVersion.HBASE1_TABLE_INTERFACE_NAME.equals(name)) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }
        };

        int version = HbaseVersion.getVersion(childCL);
        Assertions.assertEquals(HbaseVersion.HBASE_VERSION_0, version);
    }
}