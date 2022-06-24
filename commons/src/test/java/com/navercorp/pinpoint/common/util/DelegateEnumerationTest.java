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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DelegateEnumerationTest {

    @Test
    public void testNormal() {
        Hashtable<String, String> hashTable = new Hashtable<>();
        hashTable.put("a", "aa");
        hashTable.put("b", "bb");
        hashTable.put("c", "cc");

        List<String> valueList = new ArrayList<>(hashTable.values());

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertTrue(delegateEnumeration.hasMoreElements());
        Assertions.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assertions.assertTrue(delegateEnumeration.hasMoreElements());
        Assertions.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assertions.assertTrue(delegateEnumeration.hasMoreElements());
        Assertions.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assertions.assertTrue(valueList.isEmpty());

        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
        Assertions.assertNull(delegateEnumeration._getNextException());
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
    }

    @Test
    public void bug69_Inefficient_exception_is_created() {
        Hashtable<String, String> hashTable = new Hashtable<>();

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
        Assertions.assertNull(delegateEnumeration._getNextException());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
    }

    @Test
    public void bug69_Inefficient_exception_is_created_nextElement() {

        Enumeration<String> enumeration = mock(Enumeration.class);
        when(enumeration.hasMoreElements()).thenReturn(true);
        when(enumeration.nextElement()).thenReturn(null);

        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertNull(delegateEnumeration.nextElement());
        verify(enumeration, times(1)).nextElement();

        Assertions.assertNull(delegateEnumeration.nextElement());
        verify(enumeration, times(2)).nextElement();

        Assertions.assertNull(delegateEnumeration.nextElement());
        verify(enumeration, times(3)).nextElement();
    }


    @Test
    public void testSkip() {
        Hashtable<String, String> hashTable = new Hashtable<>();
        hashTable.put("a", "aa");
        hashTable.put("b", "bb");
        hashTable.put("c", "cc");

        List<String> valueList = new ArrayList<>(hashTable.values());

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration, new DelegateEnumeration.Filter<String>() {
            @Override
            public boolean filter(String s) {
                if ("bb".equals(s)) {
                    return true;
                }
                return false;
            }
        });

        Assertions.assertTrue(delegateEnumeration.hasMoreElements());
        Assertions.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assertions.assertTrue(delegateEnumeration.hasMoreElements());
        Assertions.assertTrue(valueList.remove(delegateEnumeration.nextElement()));

        Assertions.assertEquals(valueList.size(), 1);

        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assertions.assertEquals(valueList.size(), 1);

        Assertions.assertEquals(valueList.get(0), "bb");
    }

    @Test
    public void testExceptionTest_Exception() {
        Hashtable<String, String> hashTable = new Hashtable<>();

        Enumeration<String> enumeration = hashTable.elements();
        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
    }

    @Test
    public void testExceptionTest_Exception2() {

        Enumeration enumeration = mock(Enumeration.class);

        when(enumeration.hasMoreElements()).thenReturn(false);
        when(enumeration.nextElement()).thenThrow(new NoSuchElementException());

        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());
        Assertions.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assertions.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());

        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        assertNextElements_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
        Assertions.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());
    }

    @Test
    public void testExceptionTest_Null() {
        Enumeration<String> enumeration = mock(Enumeration.class);

        when(enumeration.hasMoreElements()).thenReturn(false);
        when(enumeration.nextElement()).thenReturn(null);


        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertFalse(delegateEnumeration.hasMoreElements());
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());


        Assertions.assertSame(delegateEnumeration.nextElement(), null);
        Assertions.assertSame(delegateEnumeration.nextElement(), null);
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());

    }

    @Test
    public void testExceptionTest_Null2() {
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


        DelegateEnumeration<String> delegateEnumeration = new DelegateEnumeration<>(enumeration);

        Assertions.assertTrue(delegateEnumeration.hasMoreElements());
        Assertions.assertTrue(delegateEnumeration.hasMoreElements());

        Assertions.assertSame(delegateEnumeration.nextElement(), "exist");
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());

        Assertions.assertSame(delegateEnumeration.nextElement(), null);
        Assertions.assertSame(delegateEnumeration.nextElement(), null);
        Assertions.assertFalse(delegateEnumeration.hasMoreElements());

    }


    private void assertNextElements_Expected_ExceptionEmulation(Enumeration<String> elements, DelegateEnumeration<String> delegateEnumeration) {
        Exception original = getException(elements);
        Assertions.assertNotSame(original, null);

        Exception delegate = getException(delegateEnumeration);
        Assertions.assertNotSame(delegate, null);

        Assertions.assertEquals(original.getClass(), delegate.getClass());
        Assertions.assertEquals(original.getMessage(), delegate.getMessage());
        Assertions.assertEquals(original.getCause(), delegate.getCause());
    }


    private Exception getException(Enumeration elements) {
        try {
            elements.nextElement();
        } catch (Exception e) {
            return e;
        }
        Assertions.fail("NoSuchElementException");
        return null;
    }

}