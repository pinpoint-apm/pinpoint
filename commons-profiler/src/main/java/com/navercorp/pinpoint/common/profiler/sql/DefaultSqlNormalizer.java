/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.sql;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.navercorp.pinpoint.common.profiler.sql.ParserContext.lookAhead1;
import static com.navercorp.pinpoint.common.profiler.sql.ParserContext.readLine;
import static com.navercorp.pinpoint.common.profiler.sql.ParserContext.readMultiLineComment;
import static com.navercorp.pinpoint.common.profiler.sql.Tokens.NEXT_TOKEN_NOT_EXIST;
import static com.navercorp.pinpoint.common.profiler.sql.Tokens.NUMBER_REPLACE;
import static com.navercorp.pinpoint.common.profiler.sql.Tokens.SYMBOL_REPLACE;

/**
 * @author emeroad
 */
public class DefaultSqlNormalizer implements SqlNormalizer {

    private static final NormalizedSql NULL_OBJECT = new DefaultNormalizedSql("", "");

    private final boolean removeComments;

    public DefaultSqlNormalizer() {
        this(false);
    }

    public DefaultSqlNormalizer(boolean removeComments) {
        this.removeComments = removeComments;
    }

    @Override
    public NormalizedSql normalizeSql(final String sql) {
        if (sql == null) {
            return NULL_OBJECT;
        }
        ParserContext parserContext = new ParserContext(sql, removeComments);
        return parserContext.parse();
    }


    @Override
    public String combineOutputParams(String sql, IndexedSupplier<String> outputParams) {
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
                        i = readMultiLineComment("/*", i, sql, normalized);
                        break;
                        // single line comment
                    } else if (lookAhead1Char == '/') {
                        i = readLine("//", i, sql, normalized);
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
                    if (lookAhead1(sql, i, '-')) {
                        i = readLine("--", i, sql, normalized);
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
                                    normalized.append(outputIndex);
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
                                    appendNumberToken(normalized, outputIndex);
                                    break tokenEnd;
                                }
                                try {
                                    String replaceNumber = outputParams.get(numberIndex);
                                    normalized.append(replaceNumber);
                                } catch (IndexOutOfBoundsException e) {
                                    // just append for invalid parameters
                                    appendNumberToken(normalized, outputIndex);
                                    break tokenEnd;
                                }
                                break tokenEnd;

                            case SYMBOL_REPLACE:
                                int symbolIndex = 0;
                                try {
                                    symbolIndex = Integer.parseInt(outputIndex.toString());
                                } catch (NumberFormatException e) {
                                    // just append for invalid parameters
                                    appendSymbolToken(normalized, outputIndex);
                                }
                                try {
                                    String replaceSymbol = outputParams.get(symbolIndex);
                                    normalized.append(replaceSymbol);
                                } catch (IndexOutOfBoundsException e) {
                                    appendSymbolToken(normalized, outputIndex);
                                }
                                break tokenEnd;

                            default:
                                // should look at the token outside the loop - not here
//                                    outputParam.append(SEPARATOR);
                                normalized.append(outputIndex);
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


    private void appendNumberToken(StringBuilder builder, StringBuilder outputIndex) {
        builder.append(outputIndex);
        builder.append(NUMBER_REPLACE);
    }

    private void appendSymbolToken(StringBuilder normalized, StringBuilder outputIndex) {
        normalized.append(outputIndex);
        normalized.append(SYMBOL_REPLACE);
    }

    @Override
    public String combineBindValues(String sql, List<String> bindValues) {
        if (StringUtils.isEmpty(sql)) {
            return sql;
        }
        if (CollectionUtils.isEmpty(bindValues)) {
            return sql;
        }

        final Queue<String> bindValueQueue = new LinkedList<>(bindValues);
        final int length = sql.length();
        final StringBuilder result = new StringBuilder(length + 16);

        boolean inQuotes = false;
        char quoteChar = 0;
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            if (inQuotes) {
                if (((ch == '\'') || (ch == '"')) && ch == quoteChar) {
                    if (lookAhead1(sql, i, quoteChar)) {
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
                        i = readMultiLineComment("/*", i, sql, result);
                        // single line comment
                    } else if (lookAhead1Char == '/') {
                        i = readLine("//", i, sql, result);
                    } else {
                        // unary operator
                        result.append(ch);
                    }
                } else if (ch == '-') {
                    // single line comment state
                    if (lookAhead1(sql, i) == '-') {
                        i = readLine("--", i, sql, result);
                    } else {
                        // unary operator
                        result.append(ch);
                    }
                } else if (ch == '\'' || ch == '"') {
                    inQuotes = true;
                    quoteChar = ch;
                    result.append(ch);
                } else if (ch == '?') {
                    if (!bindValueQueue.isEmpty()) {
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