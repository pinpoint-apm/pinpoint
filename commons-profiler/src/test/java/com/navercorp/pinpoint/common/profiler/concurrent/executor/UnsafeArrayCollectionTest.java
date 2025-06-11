package com.navercorp.pinpoint.common.profiler.concurrent.executor;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UnsafeArrayCollectionTest {

    @Test
    void iterator() {
        Collection<Integer> collection = new UnsafeArrayCollection<>(10);
        collection.add(1);
        collection.add(2);
        collection.add(3);

        Iterator<Integer> iterator = collection.iterator();
        assertEquals(1, iterator.next());
        assertEquals(2, iterator.next());
        assertEquals(3, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void toArray() {
        Collection<Integer> collection = new UnsafeArrayCollection<>(10);
        collection.add(1);
        collection.add(2);
        collection.add(3);

        Object[] array = collection.toArray();
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
        // no copy array
        assertEquals(10, array.length);
    }

    @Test
    void testToArray() {
        Collection<Integer> collection = new UnsafeArrayCollection<>(10);
        collection.add(1);
        collection.add(2);
        collection.add(3);

        Integer[] array = collection.toArray(new Integer[0]);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
        assertEquals(3, array.length);
    }

    @Test
    void testToArray2() {
        Collection<Integer> collection = new UnsafeArrayCollection<>(10);
        collection.add(1);
        collection.add(2);
        collection.add(3);

        Integer[] array = collection.toArray(new Integer[3]);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
        assertEquals(3, array.length);
    }

    @Test
    void testToArray3() {
        Collection<Integer> collection = new UnsafeArrayCollection<>(10);
        collection.add(1);
        collection.add(2);
        collection.add(3);

        final int arraySize = 5;
        Integer[] integers = new Integer[arraySize];
        fill(integers);

        Integer[] array = collection.toArray(integers);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
        assertEquals(arraySize, array.length);
    }

    private void fill(Integer[] integers) {
        for (int i = 0; i < 5; i++) {
            integers[i] = i + 1; // Fill with dummy data
        }
    }
}