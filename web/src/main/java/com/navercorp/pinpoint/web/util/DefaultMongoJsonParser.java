/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * @author Roy Kim
 */
public class DefaultMongoJsonParser implements MongoJsonParser {

    private static final int NEXT_TOKEN_NOT_EXIST = -1;

    public DefaultMongoJsonParser() {
    }

    private int lookAhead(String json, int index, int gap) {
        index += gap;
        if (index < json.length()) {
            return json.charAt(index);
        } else {
            return NEXT_TOKEN_NOT_EXIST;
        }
    }

    private int lookFoward(String json, int index, int gap) {
        index -= gap;
        if (index < json.length() && 0 <= index) {
            return json.charAt(index);
        } else {
            return NEXT_TOKEN_NOT_EXIST;
        }
    }

    public String combineBindValues(String json, List<String> bindValues) {
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        if (CollectionUtils.isEmpty(bindValues)) {
            return json;
        }

        int additionalSize = 0;
        final Queue<String> bindValueQueue = new LinkedList<String>();
        for (String value : bindValues) {
            bindValueQueue.add(value);
            additionalSize += value.length();
            additionalSize -= 3;
        }
        final int length = json.length();
        final StringBuilder result = new StringBuilder(length + additionalSize);

        Stack<Character> stack = new Stack<Character>();

        boolean statusKey = true;
        boolean inString = false;

        for (int i = 0; i < length; i++) {
            final char ch = json.charAt(i);

            if (statusKey && ch == '\"' && lookFoward(json, i, 1) != '\\') {
                inString = !inString;
            }

            if (!inString) {
                if (statusKey && ch == ':') {
                    statusKey = false;
                } else if (ch == '{') {
                    statusKey = true;
                    stack.push(ch);
                } else if (ch == '}' && !stack.empty()) {
                    stack.pop();
                    if (!stack.empty()){
                        if(stack.peek() != '[') {
                            statusKey = true;
                        } else if(stack.peek() == '[') {
                            statusKey = false;
                        }
                    }
                } else if (ch == '[') {
                    stack.push(ch);
                } else if (ch == ']' && !stack.empty()) {
                    stack.pop();
                    if (!stack.empty() && stack.peek() != '[') {
                        statusKey = true;
                    }
                }
            }
            if (!statusKey) {
                if (ch == '\"' && lookAhead(json, i, 1) == '?' && lookAhead(json, i, 2) == '\"') {
                    if (!bindValueQueue.isEmpty()) {
                        result.append(bindValueQueue.poll());
                        i += 2;
                        if (!stack.empty() && stack.peek() != '[') {
                            statusKey = true;
                        }
                    }
                } else {
                    result.append(ch);
                }
            } else {
                result.append(ch);
            }

        }
        return result.toString();
    }
}