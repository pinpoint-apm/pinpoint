package com.profiler.common.util;

/**
 *
 */
public class SqlUtils {
    public static final char SYMBOL_REPLACE = '$';

    public static final String normalizedSql(String sql, StringBuilder outputParam) {
        if (sql == null) {
            return "";
        }

        final int length = sql.length();
        final StringBuilder normalized = new StringBuilder(length);

        boolean newTokenState = true;
        for (int i = 0; i < length; i++) {
            final char ch = sql.charAt(i);
            switch (ch) {
                // COMMENT start check
                case '/':
                    if (lookAhead1(sql, i) == '*') {
                        // comment state
                        normalized.append("/*");
                        int stateIndex = i + 2;
                        for (; stateIndex < length; stateIndex++) {
                            char stateCh = sql.charAt(stateIndex);
                            if (stateCh == '*') {
                                if (lookAhead1(sql, stateIndex) == '/') {
                                    normalized.append("*/");
                                    break;
                                }
                            }
                            normalized.append(stateCh);
                        }
                        i = stateIndex + 1;
                        break;
                    } else {
                        // unary operator
                        newTokenState = true;
                        normalized.append(ch);
                        break;
                    }

                case '-':
                    // single line comment state
                    if (lookAhead1(sql, i) == '-') {
                        normalized.append("--");
                        int stateIndex = i + 2;
                        for (; stateIndex < length; stateIndex++) {
                            char stateCh = sql.charAt(stateIndex);
                            if (stateCh == '\n') {
                                normalized.append(stateCh);
                                break;
                            }
                            normalized.append(stateCh);
                        }
                        i = stateIndex;
                        break;
                    } else {
                        // unary operator
                        newTokenState = true;
                        normalized.append(ch);
                        break;
                    }

                    // SYMBOL start check
                case '\'':
                    // empty symbol
                    if (lookAhead1(sql, i) == '\'') {
                        normalized.append("''");
                        i += 2;
                        break;
                    } else {
                        normalized.append('\'');
                        int stateIndex = i + 1;
                        for (; stateIndex < length; stateIndex++) {
                            char stateCh = sql.charAt(stateIndex);
                            if (stateCh == '\'') {
                                if (lookAhead1(sql, stateIndex) == '\'') {
                                    stateIndex++;
                                    outputParam.append("''");
                                    continue;
                                } else {
                                    normalized.append(SYMBOL_REPLACE);
                                    normalized.append('\'');
                                    outputParam.append(',');
                                    break;
                                }
                            }
                            outputParam.append(stateCh);
                        }
                        i = stateIndex;
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
                    if (newTokenState) {
                        normalized.append('#');
                        outputParam.append(ch);
                        int stateIndex = i + 1;
                        tokenEnd:
                        for (; stateIndex < length; stateIndex++) {
                            char stateCh = sql.charAt(stateIndex);
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
                                    outputParam.append(stateCh);
                                    break;
                                default:
                                    stateIndex--;
                                    break tokenEnd;
                            }
                        }
                        i = stateIndex;
                        break;
                    } else {
                        normalized.append(ch);
                        break;
                    }

                    // 공백 space를 만남
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    newTokenState = true;
                    normalized.append(ch);
                    break;

                case '*':
                case '+':
                case '=':
                    newTokenState = true;
                    normalized.append(ch);
                    break;

                default:
                    if (ch >= 'a' && ch <= 'z') {
                        newTokenState = false;
                    } else if (ch >= 'A' && ch <= 'Z') {
                        newTokenState = false;
                    } else if (ch == '_') {
                        newTokenState = false;
                    } else {
                        newTokenState = true;
                    }
                    normalized.append(ch);
                    break;
            }
        }

        return normalized.toString();
    }

    /**
     * 미리 다음 문자열 하나를 까본다.
     *
     * @param sql
     * @param index
     * @return
     */
    private static int lookAhead1(String sql, int index) {
        index++;
        if (index < sql.length()) {
            return sql.charAt(index);
        } else {
            return -1;
        }
    }

}
