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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author emeroad
 */
public class DefaultSqlParser implements SqlParser {

    public static final char SEPARATOR = ',';
    public static final char SYMBOL_REPLACE = '$';
    public static final char NUMBER_REPLACE = '#';


    private static final int NEXT_TOKEN_NOT_EXIST = -1;
    private static final int NORMALIZED_SQL_BUFFER = 32;

    private static final NormalizedSql NULL_OBJECT = new DefaultNormalizedSql("", "");

    public DefaultSqlParser() {
    }


    @Override
    public NormalizedSql normalizedSql(final String sql) {
        if (sql == null) {
            return NULL_OBJECT;
        }

        final int length = sql.length();
        final StringBuilder normalized = new StringBuilder(length + NORMALIZED_SQL_BUFFER);
        final StringBuilder parsedParameter = new StringBuilder(32);
        boolean change = false;
        int replaceIndex = 0;
        boolean numberTokenStartEnable = true;
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            switch (ch) {
                // COMMENT start check
                case '/':
                    // comment state
                    final int lookAhead1Char = lookAhead1(sql, i);
                    // multi line comment and oracle hint /*+ */
                    if (lookAhead1Char == '*') {
                        normalized.append("/*");
                        i += 2;
                        for (; i < length; i++) {
                            char stateCh = sql.charAt(i);
                            if (stateCh == '*') {
                                if (lookAhead1(sql, i) == '/') {
                                    normalized.append("*/");
                                    i++;
                                    break;
                                }
                            }
                            normalized.append(stateCh);
                        }
                        break;
                        // single line comment
                    } else if (lookAhead1Char == '/') {
                        normalized.append("//");
                        i += 2;
                        i = readLine(sql, normalized, i);
                        break;

                    } else {
                        // unary operator
                        numberTokenStartEnable = true;
                        normalized.append(ch);
                        break;
                    }
//                case '#'
//                    # is a single line comment in mysql
                case '-':
                    // single line comment state
                    if (lookAhead1(sql, i) == '-') {
                        normalized.append("--");
                        i += 2;
                        i = readLine(sql, normalized, i);
                        break;
                    } else {
                        // unary operator
                        numberTokenStartEnable = true;
                        normalized.append(ch);
                        break;
                    }

                    // SYMBOL start check
                case '\'':
                    // empty symbol
                    if (lookAhead1(sql, i) == '\'') {
                        normalized.append("''");
                        // no need to add parameter to output as $ is not converted
                        i += 2;
                        break;
                    } else {
                        change = true;
                        normalized.append('\'');
                        i++;
                        appendOutputSeparator(parsedParameter);
                        for (; i < length; i++) {
                            char stateCh = sql.charAt(i);
                            if (stateCh == '\'') {
                                // a consecutive ' is the same as \'
                                if (lookAhead1(sql, i) == '\'') {
                                    i++;
                                    appendOutputParam(parsedParameter, "''");
                                    continue;
                                } else {
                                    normalized.append(replaceIndex++);
                                    normalized.append(SYMBOL_REPLACE);
                                    normalized.append('\'');
//                                    outputParam.append(',');
                                    break;
                                }
                            }
                            appendSeparatorCheckOutputParam(parsedParameter, stateCh);
                        }
                        break;
                    }

                    // number start check
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    // http://www.h2database.com/html/grammar.html look at the state machine more
                    if (numberTokenStartEnable) {
                        change = true;
                        normalized.append(replaceIndex++);
                        normalized.append(NUMBER_REPLACE);
                        // number token start
                        appendOutputSeparator(parsedParameter);
                        appendOutputParam(parsedParameter, ch);
                        i++;
                        tokenEnd:
                        for (; i < length; i++) {
                            char stateCh = sql.charAt(i);
                            switch (stateCh) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                case '.':
                                case 'E':
                                case 'e':
                                    appendOutputParam(parsedParameter, stateCh);
                                    break;
                                default:
                                    // should look at the token outside the loop - not here
//                                    outputParam.append(SEPARATOR);
                                    i--;
                                    break tokenEnd;
                            }
                        }
                        break;
                    } else {
                        normalized.append(ch);
                        break;
                    }

                    // empty space
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    numberTokenStartEnable = true;
                    normalized.append(ch);
                    break;
                // http://msdn.microsoft.com/en-us/library/ms174986.aspx
                case '*':
                case '+':
                case '%':
                case '=':
                case '<':
                case '>':
                case '&':
                case '|':
                case '^':
                case '~':
                case '!':
                    numberTokenStartEnable = true;
                    normalized.append(ch);
                    break;

                case '(':
                case ')':
                case ',':
                case ';':
                    numberTokenStartEnable = true;
                    normalized.append(ch);
                    break;

                case '.':
                case '_':
                case '@': // Assignment Operator
                case ':': // Oracle's bind variable is possible with :bindvalue
                    numberTokenStartEnable = false;
                    normalized.append(ch);
                    break;

                default:
                    // what if it's in a different language??
                    if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                        numberTokenStartEnable = false;
                    } else {
                        numberTokenStartEnable = true;
                    }
                    normalized.append(ch);
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

            return new DefaultNormalizedSql(normalized.toString(), parsedParameterString);
        } else {
            // Reuse if not modified.
            // 1. new strings are not generated
            // 2. reuse hashcodes
            return new DefaultNormalizedSql(sql, "");
        }
    }

    private int readLine(String sql, StringBuilder normalized, int index) {
        final int length = sql.length();
        for (; index < length; index++) {
            char ch = sql.charAt(index);
            normalized.append(ch);
            if (ch == '\n') {
                break;
            }
        }
        return index;
    }

    private void appendOutputSeparator(StringBuilder output) {
        if (output.length() == 0) {
            // first parameter
            return;
        }
        output.append(SEPARATOR);
    }

    private void appendOutputParam(StringBuilder output, String str) {
        output.append(str);
    }

    private void appendSeparatorCheckOutputParam(StringBuilder output, char ch) {
        if (ch == ',') {
            output.append(",,");
        } else {
            output.append(ch);
        }
    }

    private void appendOutputParam(StringBuilder output, char ch) {
        output.append(ch);
    }

    /**
     * look up the next character in a string
     *
     * @param sql
     * @param index
     * @return
     */
    private int lookAhead1(String sql, int index) {
        index++;
        if (index < sql.length()) {
            return sql.charAt(index);
        } else {
            return NEXT_TOKEN_NOT_EXIST;
        }
    }

    @Override
    public String combineOutputParams(String sql, List<String> outputParams) {

        final int length = sql.length();
        final StringBuilder normalized = new StringBuilder(length + 16);
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            switch (ch) {
                // COMMENT start check
                case '/':
                    // comment state
                    int lookAhead1Char = lookAhead1(sql, i);
                    // multi line comment and oracle hint /*+ */
                    if (lookAhead1Char == '*') {
                        normalized.append("/*");
                        i += 2;
                        for (; i < length; i++) {
                            char stateCh = sql.charAt(i);
                            if (stateCh == '*') {
                                if (lookAhead1(sql, i) == '/') {
                                    normalized.append("*/");
                                    i++;
                                    break;
                                }
                            }
                            normalized.append(stateCh);
                        }
                        break;
                        // single line comment
                    } else if (lookAhead1Char == '/') {
                        normalized.append("//");
                        i += 2;
                        i = readLine(sql, normalized, i);
                        break;

                    } else {
                        // unary operator
//                        numberTokenStartEnable = true;
                        normalized.append(ch);
                        break;
                    }
//                case '#'
//                  # is a single line comment in mysql
                case '-':
                    // single line comment state
                    if (lookAhead1(sql, i) == '-') {
                        normalized.append("--");
                        i += 2;
                        i = readLine(sql, normalized, i);
                        break;
                    } else {
                        // unary operator
//                        numberTokenStartEnable = true;
                        normalized.append(ch);
                        break;
                    }

                    // number start check
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    // http://www.h2database.com/html/grammar.html look at the state machine more
                    if (lookAhead1(sql, i) == NEXT_TOKEN_NOT_EXIST) {
                        normalized.append(ch);
                        break;
                    }
                    StringBuilder outputIndex = new StringBuilder();
                    outputIndex.append(ch);
                    // number token start
                    i++;
                    tokenEnd:
                    for (; i < length; i++) {
                        final char stateCh = sql.charAt(i);
                        switch (stateCh) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                if (lookAhead1(sql, i) == NEXT_TOKEN_NOT_EXIST) {
                                    outputIndex.append(stateCh);
                                    normalized.append(outputIndex.toString());
                                    break tokenEnd;
                                }
                                outputIndex.append(stateCh);
                                break;
                            case NUMBER_REPLACE:
                                int numberIndex = 0;
                                try {
                                    numberIndex = Integer.parseInt(outputIndex.toString());
                                } catch (NumberFormatException e) {
                                    // just append for invalid parameters
                                    normalized.append(outputIndex.toString());
                                    normalized.append(NUMBER_REPLACE);
                                    break tokenEnd;
                                }
                                try {
                                    String replaceNumber = outputParams.get(numberIndex);
                                    normalized.append(replaceNumber);
                                } catch (IndexOutOfBoundsException e) {
                                    // just append for invalid parameters
                                    normalized.append(outputIndex.toString());
                                    normalized.append(NUMBER_REPLACE);
                                    break tokenEnd;
                                }
                                break tokenEnd;

                            case SYMBOL_REPLACE:
                                int symbolIndex = 0;
                                try {
                                    symbolIndex = Integer.parseInt(outputIndex.toString());
                                } catch (NumberFormatException e) {
                                    // just append for invalid parameters
                                    normalized.append(outputIndex.toString());
                                    normalized.append(SYMBOL_REPLACE);
                                }
                                try {
                                    String replaceSymbol = outputParams.get(symbolIndex);
                                    normalized.append(replaceSymbol);
                                } catch (IndexOutOfBoundsException e) {
                                    normalized.append(outputIndex.toString());
                                    normalized.append(SYMBOL_REPLACE);
                                }
                                break tokenEnd;

                            default:
                                // should look at the token outside the loop - not here
//                                    outputParam.append(SEPARATOR);
                                normalized.append(outputIndex.toString());
                                i--;
                                break tokenEnd;
                        }
                    }
                    break;

                default:
                    normalized.append(ch);
                    break;
            }
        }

        return normalized.toString();
    }

    public String combineBindValues(String sql, List<String> bindValues) {
        if (StringUtils.isEmpty(sql)) {
            return sql;
        }
        if (CollectionUtils.isEmpty(bindValues)) {
            return sql;
        }

        final Queue<String> bindValueQueue = new LinkedList<String>();
        for(String value : bindValues) {
            // trim
            bindValueQueue.add(value.trim());
        }
        final int length = sql.length();
        final StringBuilder result = new StringBuilder(length + 16);

        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            if (inQuotes) {
                if (((ch == '\'') || (ch == '"')) && ch == quoteChar) {
                    if (lookAhead1(sql, i) == quoteChar) {
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
                // COMMENT start check
                if (ch == '/') {
                    // comment state
                    int lookAhead1Char = lookAhead1(sql, i);
                    // multi line comment and oracle hint /*+ */
                    if (lookAhead1Char == '*') {
                        result.append("/*");
                        i += 2;
                        for (; i < length; i++) {
                            char stateCh = sql.charAt(i);
                            if (stateCh == '*') {
                                if (lookAhead1(sql, i) == '/') {
                                    result.append("*/");
                                    i++;
                                    break;
                                }
                            }
                            result.append(stateCh);
                        }
                        // single line comment
                    } else if (lookAhead1Char == '/') {
                        result.append("//");
                        i += 2;
                        i = readLine(sql, result, i);
                    } else {
                        // unary operator
                        result.append(ch);
                    }
                } else if (ch == '-') {
                    // single line comment state
                    if (lookAhead1(sql, i) == '-') {
                        result.append("--");
                        i += 2;
                        i = readLine(sql, result, i);
                    } else {
                        // unary operator
                        result.append(ch);
                    }
                } else if (ch == '\'' || ch == '"') {
                    inQuotes = true;
                    quoteChar = ch;
                    result.append(ch);
                } else if(ch == '?') {
                    if(!bindValueQueue.isEmpty()) {
                        result.append('\'').append(bindValueQueue.poll()).append('\'');
                    }
                } else {
                    result.append(ch);
                }
            }
        }

        return result.toString();
    }
}