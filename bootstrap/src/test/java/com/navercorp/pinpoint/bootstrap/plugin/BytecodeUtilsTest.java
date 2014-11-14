package com.nhn.pinpoint.bootstrap.plugin;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class BytecodeUtilsTest {

    @Test
    public void testDefineClass() throws Exception {

    }

    @Test
    public void testGetClassFile() throws Exception {


    }

    @Test
    public void testGetClassFile_SystemClassLoader() {
        // SystemClassLoader class
        Class<String> stringClass = String.class;
        byte[] stringClassBytes = BytecodeUtils.getClassFile(stringClass.getClassLoader(), stringClass.getName());
        Assert.assertNotNull(stringClassBytes);
    }
}