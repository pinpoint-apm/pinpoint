/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class DelegateEnumerationTest {

    @Test
    public void testNormal() throws Exception {
        Hashtable<String, String> hashTable = new Hashtable<String, String>();
        hashTable.put("a", "aa");
        hashTable.put("b", "bb");
        hashTable.put("c", "cc");

        List<String> valueList = new ArrayList<String>(hashTable.values());

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertTrue(delegateEnumeration.hasMoreElements());
        Assert.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assert.assertTrue(delegateEnumeration.hasMoreElements());
        Assert.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assert.assertTrue(delegateEnumeration.hasMoreElements());
        Assert.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assert.assertTrue(valueList.isEmpty());

        Assert.assertFalse(delegateEnumeration.hasMoreElements());
        Assert.assertNull(delegateEnumeration._getNextException());
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
    }

    @Test
    public void bug69_Inefficient_exception_is_created() throws Exception {
        Hashtable<String, String> hashTable = new Hashtable<String, String>();

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertFalse(delegateEnumeration.hasMoreElements());
        Assert.assertNull(delegateEnumeration._getNextException());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
    }

    @Test
    public void bug69_Inefficient_exception_is_created_nextElement() throws Exception {

        Enumeration<String> enumeration = mock(Enumeration.class);
        when(enumeration.hasMoreElements()).thenReturn(true);
        when(enumeration.nextElement()).thenReturn(null);

        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertNull(delegateEnumeration.nextElement());
        verify(enumeration, times(1)).nextElement();

        Assert.assertNull(delegateEnumeration.nextElement());
        verify(enumeration, times(2)).nextElement();

        Assert.assertNull(delegateEnumeration.nextElement());
        verify(enumeration, times(3)).nextElement();
    }


    @Test
    public void testSkip() throws Exception {
        Hashtable<String, String> hashTable = new Hashtable<String, String>();
        hashTable.put("a", "aa");
        hashTable.put("b", "bb");
        hashTable.put("c", "cc");

        List<String> valueList = new ArrayList<String>(hashTable.values());

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration, new DelegateEnumeration.Filter<String>() {
            @Override
            public boolean filter(String s) {
                if ("bb".equals(s)) {
                    return true;
                }
                return false;
            }
        });

        Assert.assertTrue(delegateEnumeration.hasMoreElements());
        Assert.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assert.assertTrue(delegateEnumeration.hasMoreElements());
        Assert.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assert.assertEquals(valueList.size(), 1);

        Assert.assertFalse(delegateEnumeration.hasMoreElements());
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assert.assertEquals(valueList.size(), 1);

        Assert.assertEquals(valueList.get(0), "bb");
    }

    @Test
    public void testExceptionTest_Exception() throws Exception {
        Hashtable<String, String> hashTable = new Hashtable<String, String>();

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertFalse(delegateEnumeration.hasMoreElements());
        Assert.assertFalse(delegateEnumeration.hasMoreElements());
        Assert.assertFalse(delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assert.assertFalse(delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assert.assertFalse(delegateEnumeration.hasMoreElements());
    }

    @Test
    public void testExceptionTest_Exception2() throws Exception {

        Enumeration enumeration = mock(Enumeration.class);

        when(enumeration.hasMoreElements()).thenReturn(false);
        when(enumeration.nextElement()).thenThrow(new NoSuchElementException());

        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());
        Assert.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assert.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assert.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());
    }

    @Test
    public void testExceptionTest_Null() throws Exception {
        Enumeration enumeration = mock(Enumeration.class);

        when(enumeration.hasMoreElements()).thenReturn(false);
        when(enumeration.nextElement()).thenReturn(null);


        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertFalse(delegateEnumeration.hasMoreElements());
        Assert.assertFalse(delegateEnumeration.hasMoreElements());


        Assert.assertSame(delegateEnumeration.nextElement(), null);
        Assert.assertSame(delegateEnumeration.nextElement(), null);
        Assert.assertFalse(delegateEnumeration.hasMoreElements());

    }

    @Test
    public void testExceptionTest_Null2() throws Exception {
        Enumeration<String> enumeration = new Enumeration<String>() {
            private boolean first = true;
            @Override
            public boolean hasMoreElements() {
                return first;
            }

            @Override
            public String nextElement() {
                if (first) {
                    first = false;
                    return "exist";
                }
                return null;
            }
        };


        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<String>(enumeration);

        Assert.assertTrue(delegateEnumeration.hasMoreElements());
        Assert.assertTrue(delegateEnumeration.hasMoreElements());

        Assert.assertSame(delegateEnumeration.nextElement(), "exist");
        Assert.assertFalse(delegateEnumeration.hasMoreElements());

        Assert.assertSame(delegateEnumeration.nextElement(), null);
        Assert.assertSame(delegateEnumeration.nextElement(), null);
        Assert.assertFalse(delegateEnumeration.hasMoreElements());

    }


    private void assertNextElements_Expected_ExceptionEmulation(Enumeration<String> elements, DelegateEnumeration<String> delegateEnumeration) {
        Exception original = getException(elements);
        Assert.assertNotSame(original, null);

        Exception delegate = getException(delegateEnumeration);
        Assert.assertNotSame(delegate, null);

        Assert.assertEquals(original.getClass(), delegate.getClass());
        Assert.assertEquals(original.getMessage(), delegate.getMessage());
        Assert.assertEquals(original.getCause(), delegate.getCause());
    }


    private Exception getException(Enumeration elements) {
        try {
            elements.nextElement();
        } catch (Exception e) {
            return e;
        }
        Assert.fail("NoSuchElementException");
        return null;
    }

}