package com.nhn.pinpoint.common.util;

import java.util.List;

/**
 * @author emeroad
 */
public class SqlParser {

    public static final char SYMBOL_REPLACE = '$';
    public static final char NUMBER_REPLACE = '#';

    private static final DefaultParsingResult NULL = new DefaultParsingResult("", new StringBuilder());
    private static final int NEXT_TOKEN_NOT_EXIST = -1;


    public SqlParser() {
    }

    public DefaultParsingResult normalizedSql(String sql) {
        if (sql == null) {
            return NULL;
        }

        DefaultParsingResult parsingResult = new DefaultParsingResult();
        final int length = sql.length();
        final StringBuilder normalized = new StringBuilder(length + 16);
        boolean change = false;
        int replaceIndex = 0;
        boolean numberTokenStartEnable = true;
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
                        numberTokenStartEnable = true;
                        normalized.append(ch);
                        break;
                    }
//                case '#'
//                    mysql 에서는 #도 한줄 짜리 comment이다.
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
                        // $로 치환하지 않으므로 output에 파라미터를 넣을필요가 없다
                        i += 2;
                        break;
                    } else {
                        change = true;
                        normalized.append('\'');
                        i++;
                        parsingResult.appendOutputSeparator();
                        for (; i < length; i++) {
                            char stateCh = sql.charAt(i);
                            if (stateCh == '\'') {
                                // '' 이 연속으로 나왔을 경우는 \' 이므로 그대로 넣는다.
                                if (lookAhead1(sql, i) == '\'') {
                                    i++;
                                    parsingResult.appendOutputParam("''");
                                    continue;
                                } else {
                                    normalized.append(replaceIndex++);
                                    normalized.append(SYMBOL_REPLACE);
                                    normalized.append('\'');
//                                    outputParam.append(',');
                                    break;
                                }
                            }
                            parsingResult.appendSeparatorCheckOutputParam(stateCh);
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
                    // http://www.h2database.com/html/grammar.html 추가로 state machine을 더볼것.
                    if (numberTokenStartEnable) {
                        change = true;
                        normalized.append(replaceIndex++);
                        normalized.append(NUMBER_REPLACE);
                        // number token start
                        parsingResult.appendOutputSeparator();
                        parsingResult.appendOutputParam(ch);
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
                                    parsingResult.appendOutputParam(stateCh);
                                    break;
                                default:
                                    // 여기서 처리하지 말고 루프 바깥으로 나가서 다시 token을 봐야 된다.
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

                    // 공백 space를 만남
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    numberTokenStartEnable = true;
                    normalized.append(ch);
                    break;
                // http://msdn.microsoft.com/en-us/library/ms174986.aspx 참조.
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
                case ':': // 오라클쪽의 bind 변수는 :bindvalue로도 가능.
                    numberTokenStartEnable = false;
                    normalized.append(ch);
                    break;

                default:
                    // 한글이면 ??
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
            parsingResult.setSql(normalized.toString());
            return parsingResult;
        } else {
            // 수정되지 않았을 경우의 재활용.
            // 1. 성능향상을 위해 string을 생성하지 않도록.
            // 2. hash code재활용.
            parsingResult.setSql(sql);
            return parsingResult;
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

    /**
     * 미리 다음 문자열 하나를 까본다.
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
//                    mysql 에서는 #도 한줄 짜리 comment이다.
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
                    // http://www.h2database.com/html/grammar.html 추가로 state machine을 더볼것.
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
                                    // 잘못된 파라미터일 경우 그냥 쓰자.
                                    normalized.append(outputIndex.toString());
                                    normalized.append(NUMBER_REPLACE);
                                    break tokenEnd;
                                }
                                try {
                                    String replaceNumber = outputParams.get(numberIndex);
                                    normalized.append(replaceNumber);
                                } catch (IndexOutOfBoundsException e) {
                                    // 잘못된 파라미터일 경우 그냥 쓰자.
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
                                    // 잘못된 파라미터일 경우 그냥 쓰자.
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
                                // 여기서 처리하지 말고 루프 바깥으로 나가서 다시 token을 봐야 된다.
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

}
