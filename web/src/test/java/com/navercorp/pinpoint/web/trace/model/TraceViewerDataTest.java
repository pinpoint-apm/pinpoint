/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.trace.model;

import com.navercorp.pinpoint.web.vo.callstacks.Record;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.MutableStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraceViewerDataTest {

    @Test
    @Disabled
    void stack() {
        Stack<Record> stack = new Stack<>();

        stack.push(newRecord(1));
        stack.push(newRecord(2));
        stack.push(newRecord(3));
        stack.push(newRecord(4));
        stack.push(newRecord(5));

        int index = indexOf(stack, 2);

        Assertions.assertEquals(3, index);
        Assertions.assertEquals(2, stack.size());
    }

    @Test
    void mutableStack_pop() {
        MutableStack<Record> stack = Stacks.mutable.of();

        stack.push(newRecord(1));
        stack.push(newRecord(2));
        stack.push(newRecord(3));
        stack.push(newRecord(4));
        stack.push(newRecord(5));

        int index = TraceViewerData.pop(stack, 2);

        Assertions.assertEquals(3, index);
        Assertions.assertEquals(2, stack.size());

    }

    @Test
    void mutableStack_pop_notfound() {
        MutableStack<Record> stack = Stacks.mutable.of();

        stack.push(newRecord(1));
        stack.push(newRecord(2));

        int index = TraceViewerData.pop(stack, 10);

        Assertions.assertEquals(-1, index);
        Assertions.assertEquals(2, stack.size());

    }

    private Record newRecord(int id) {
        Record r = mock(Record.class);
        when(r.getId()).thenReturn(id);
        return r;
    }


    static int indexOf(Stack<Record> recordTrace, int id) {
        int index = 0;
        for (int i = recordTrace.size() - 1; i >= 0 ; i--) {
            Record record = recordTrace.get(i);
            if (record.getId() == id) {
                recordTrace.setSize(i + 1);
                return index;
            }
            index++;
        }
        return -1;
    }

}