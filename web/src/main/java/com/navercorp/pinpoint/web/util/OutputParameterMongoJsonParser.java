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

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author Roy Kim
 */
public class OutputParameterMongoJsonParser {

    public static final char SEPARATOR = ',';
    public static final char ARRAYMARK = '[';
    public static final char CLASSMARK = '{';

    public List<String> parseOutputParameter(String outputParams) {
        // may also need to know about the parsing result 
        if (StringUtils.isEmpty(outputParams)) {
            return Collections.emptyList();
        }

        Stack<Character> stack = new Stack<Character>();
        final List<String> result = new LinkedList<String>();
        StringBuilder params = new StringBuilder();
        int len = outputParams.length();

        for (int index = 0; index < len; index++) {
            final char ch = outputParams.charAt(index);

            if (stack.empty()) {
                if (ch == SEPARATOR) {
                    result.add(params.toString());
                    params = new StringBuilder();
                } else if (ch == ARRAYMARK || ch == CLASSMARK) {
                    stack.push(ch);
                    params.append(ch);
                } else if (ch == '\"') {
                    params.append(ch);

                    boolean breaker = false;
                    for (index = index + 1; index < len; index++) {
                        char stateCh = outputParams.charAt(index);
                        switch (stateCh) {
                            case '\"':
                                params.append(stateCh);
                                breaker = true;
                                break;
                            default:
                                params.append(stateCh);
                                break;
                        }
                        if (breaker) {
                            break;
                        }
                    }
                } else {
                    params.append(ch);
                }
            } else if (ch == ']') {
                if (stack.peek() == ARRAYMARK) {
                    stack.pop();
                    params.append(ch);
                }
            } else if (ch == '}') {
                if (stack.peek() == CLASSMARK) {
                    stack.pop();
                    params.append(ch);
                }
            } else {
                params.append(ch);
            }
        }

        result.add(params.toString());

        return result;
    }
}
