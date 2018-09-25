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

package com.navercorp.pinpoint.common.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * @author Roy Kim
 */
public class DefaultJsonParser implements JsonParser {

    public static final char SEPARATOR = ',';
    public static final char NUMBER_REPLACE = '?';


    private static final int NEXT_TOKEN_NOT_EXIST = -1;
    private static final int NORMALIZED_JSON_BUFFER = 32;

    private static final NormalizedJson NULL_OBJECT = new DefaultNormalizedJson("", "");

    public DefaultJsonParser() {
    }

    @Override
    public NormalizedJson normalizeJson(final String json) {
        if (json == null) {
            return NULL_OBJECT;
        }

        final int length = json.length();
        final StringBuilder normalized = new StringBuilder(length + NORMALIZED_JSON_BUFFER);
        final StringBuilder parsedParameter = new StringBuilder(32);
        boolean change = false;
        boolean getValue;

        Stack<Character> stack = new Stack<Character>();

        for (int i = 0; i < length; i++) {
            char ch = json.charAt(i);

            if(!stack.empty() && (stack.peek() == ':' ||  stack.peek() == '[')){
                getValue = true;
            }else{
                getValue = false;
            }

            switch (ch) {

                case ':':
                    normalized.append(ch);
                    stack.push(':');
                    //getValue = true;
                    break;

                case '{':
                    normalized.append(ch);
                    stack.push('{');
                    //getValue = false;
                    break;

                case '[':
                    normalized.append(ch);
                    stack.push('[');
                    //getValue = false;
                    break;

                case '}':
                    normalized.append(ch);
                    if(!stack.empty() && stack.peek() == '{'){
                        stack.pop();
                    }
                    break;

                case ']':
                    normalized.append(ch);
                    if(!stack.empty() && stack.peek() == '['){
                        stack.pop();
                        if(!stack.empty() && stack.peek() == ':'){
                            stack.pop();
                        }
                    }
                    break;

                case '\"':

                    if(!getValue){

                        normalized.append(ch);
                    }else{

                        appendOutputSeparator(parsedParameter);
                        appendOutputParam(parsedParameter, ch);
                        change = true;
                        if(!stack.empty() && stack.peek() == ':'){
                            stack.pop();
                        }
                        normalized.append(NUMBER_REPLACE);

                        boolean t = false;
                        for (i=i+1; i < length; i++) {
                            char stateCh = json.charAt(i);
                            switch (stateCh) {
                                case '\"':
                                    appendOutputParam(parsedParameter, stateCh);
                                    t = true;
                                    break;
                                default:
                                    appendOutputParam(parsedParameter, stateCh);
                                    break;
                            }
                            if(t){
                                break;
                            }
                        }
                    }
                    break;

                case ' ':
                    normalized.append(ch);
                    break;

                default:
                    if(!getValue){
                        normalized.append(ch);
                    }else {

                        if(!stack.empty() && stack.peek() == ':'){
                            stack.pop();
                        }

                        boolean wasValue = false;

                        appendOutputSeparator(parsedParameter);
                        for (; ch >= ' ' && ",:]}/\\\"[{;=#".indexOf(ch) < 0; ch = json.charAt(++i)) {
                            wasValue = true;
                            appendOutputParam(parsedParameter, ch);
                        }
                        if(wasValue){
                            change = true;
                            normalized.append(NUMBER_REPLACE);
                        }else{
                            if(parsedParameter.length() > 0) {
                                parsedParameter.deleteCharAt(parsedParameter.length() - 1);
                            }
                        }
                        normalized.append(ch);
                    }
                    break;
            }
        }
        if (change) {
            String parsedParameterString;
            if (parsedParameter.length() > 0) {
                parsedParameterString = parsedParameter.toString();
            } else {
                parsedParameterString = "";
            }

            return new DefaultNormalizedJson(normalized.toString(), parsedParameterString);
        } else {
            // Reuse if not modified.
            // 1. new strings are not generated
            // 2. reuse hashcodes
            return new DefaultNormalizedJson(json, "");
        }
    }

    private void appendOutputSeparator(StringBuilder output) {
        if (output.length() == 0) {
            // first parameter
            return;
        }
        output.append(SEPARATOR);
    }

    private void appendOutputParam(StringBuilder output, char ch) {
        output.append(ch);
    }

    /**
     * look up the next character in a string
     *
     * @param json
     * @param index
     * @return
     */
    private int lookAhead1(String json, int index) {
        index++;
        if (index < json.length()) {
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

        final Queue<String> bindValueQueue = new LinkedList<String>();
        for(String value : bindValues) {
            // trim
            bindValueQueue.add(value.trim());
        }
        final int length = json.length();
        final StringBuilder result = new StringBuilder(length + 16);

        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < length; i++) {
            final char ch = json.charAt(i);
            if (inQuotes) {
                if (((ch == '\'') || (ch == '"')) && ch == quoteChar) {
                    if (lookAhead1(json, i) == quoteChar) {
                        // inline quote.
                        result.append(ch);
                        i++;
                        continue;
                    }
                    inQuotes = !inQuotes;
                    quoteChar = 0;
                }
                result.append(ch);
            } else {
                if (ch == '\'' || ch == '"') {
                    inQuotes = true;
                    quoteChar = ch;
                    result.append(ch);
                } else if(ch == '?') {
                    if(!bindValueQueue.isEmpty()) {
                        result.append(bindValueQueue.poll());
                    }
                } else {
                    result.append(ch);
                }
            }
        }

        return result.toString();
    }
}