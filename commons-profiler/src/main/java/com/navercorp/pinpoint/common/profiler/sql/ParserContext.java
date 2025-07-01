package com.navercorp.pinpoint.common.profiler.sql;

import java.util.Objects;

import static com.navercorp.pinpoint.common.profiler.sql.Tokens.NEXT_TOKEN_NOT_EXIST;
import static com.navercorp.pinpoint.common.profiler.sql.Tokens.NORMALIZED_SQL_BUFFER;
import static com.navercorp.pinpoint.common.profiler.sql.Tokens.NUMBER_REPLACE;
import static com.navercorp.pinpoint.common.profiler.sql.Tokens.SYMBOL_REPLACE;

public class ParserContext {

    private final String sql;
    private final int length;

    private final StringBuilder normalized;
    // private final StringBuilder parsedParameter;
    private final ParameterBuilder parameter;

    private final boolean removeComments;

    private int replaceIndex = 0;

    public ParserContext(String sql, boolean removeComments) {
        this.sql = Objects.requireNonNull(sql, "sql");
        this.length = sql.length();
        this.removeComments = removeComments;

        this.normalized = new StringBuilder(length + NORMALIZED_SQL_BUFFER);
        // this.parsedParameter = new StringBuilder(32);
        this.parameter = new ParameterBuilder();
    }

    public NormalizedSql parse() {

        boolean numberTokenStartEnable = true;
        for (int i = 0; i < this.length; i++) {
            final char ch = sql.charAt(i);
            switch (ch) {
                // COMMENT start check
                case '/':
                    // comment state
                    final int lookAhead1Char = lookAhead1(i);
                    // multi line comment and oracle hint /*+ */
                    if (lookAhead1Char == '*') {
                        if (removeComments) {
                            this.parameter.touch();
                            i = skipComment("/*", i);
                        } else {
                            i = readComment("/*", i, normalized);
                        }
                        // single line comment
                    } else if (lookAhead1Char == '/') {
                        if (removeComments) {
                            this.parameter.touch();
                            i = skipLine("//", i, normalized);
                        } else {
                            i = readLine("//", i, normalized);
                        }
                    } else {
                        // unary operator
                        numberTokenStartEnable = true;
                        normalized.append(ch);
                    }
                    break;
                // case '#'
                // # is a single line comment in mysql
                case '-':
                    // single line comment state
                    if (lookAhead1(i) == '-') {
                        if (removeComments) {
                            this.parameter.touch();
                            i = skipLine("--", i, normalized);
                        } else {
                            i = readLine("--", i, normalized);
                        }
                    } else {
                        // unary operator
                        numberTokenStartEnable = true;
                        normalized.append(ch);
                    }
                    break;

                // SYMBOL start check
                case '\'':
                    i = readSymbol(i);
                    break;

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
                        appendNumberToken(normalized);
                        // number token start
                        parameter.separator();
                        i = readNumber(i);
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

                case '$':
                    final int nextChar = lookAhead1(i);
                    if (nextChar >= '0' && nextChar <= '9') {
                        numberTokenStartEnable = false;
                    }
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
                    numberTokenStartEnable = isNumberTokenStart(ch);
                    normalized.append(ch);
                    break;
            }
        }
        if (parameter.isChange()) {
            String parsedParameterString = parameter.build();
            return new DefaultNormalizedSql(normalized.toString(), parsedParameterString);
        } else {
            // Reuse if not modified.
            // 1. new strings are not generated
            // 2. reuse hashcodes
            return new DefaultNormalizedSql(sql, "");
        }
    }

    private int readSymbol(int i) {
        // empty symbol
        if (lookAhead1(i) == '\'') {
            normalized.append("''");
            // no need to add parameter to output as $ is not converted
            i += 1;
            return i;
        } else {
            normalized.append('\'');
            i++;
            parameter.separator();
            for (; i < this.length; i++) {
                char stateCh = sql.charAt(i);
                if (stateCh == '\'') {
                    // a consecutive ' is the same as \'
                    if (lookAhead1(i) == '\'') {
                        i++;
                        parameter.append("''");
                        continue;
                    } else {
                        appendSymbolToken(normalized);
                        normalized.append('\'');
                        // outputParam.append(',');
                        break;
                    }
                }
                parameter.appendSeparatorCheck(stateCh);
            }
            return i;
        }
    }

    private boolean isNumberTokenStart(char ch) {
        return (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z');
    }

    private int nextTokenIndex() {
        return this.replaceIndex++;
    }

    private void appendNumberToken(StringBuilder builder) {
        builder.append(nextTokenIndex());
        builder.append(NUMBER_REPLACE);
    }

    private void appendSymbolToken(StringBuilder normalized) {
        normalized.append(nextTokenIndex());
        normalized.append(SYMBOL_REPLACE);
    }

    private int readNumber(int i) {
        final int startIndex = i;
        int end = 0;

        i++;

        tokenEnd:
        for (; i < this.length; i++) {
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
                case '.':
                case 'E':
                case 'e':
                    break;
                default:
                    // should look at the token outside the loop - not here
                    // outputParam.append(SEPARATOR);
                    end = 1;
                    i--;
                    break tokenEnd;
            }
        }

        parameter.append(sql, startIndex, i + end);
        return i;
    }

    private int skipLine(String first, int index, StringBuilder normalized) {
        return readComment(first, "\n", index, this.sql, null);
    }

    private int readLine(String first, int index, StringBuilder normalized) {
        return readComment(first, "\n", index, this.sql, normalized);
    }

    public static int readLine(String first, int i, String sql, StringBuilder normalized) {
        return readComment(first, "\n", i, sql, normalized);
    }

    private int readComment(String first, int i, StringBuilder normalized) {
        return readComment(first, "*/", i, this.sql, normalized);
    }

    private int skipComment(String first, int i) {
        return readComment(first, "*/", i, this.sql, null);
    }

    public static int readComment(String firstToken, String endToken, int i, String sql, StringBuilder normalized) {
        final int startIndex = i;
        int end = 0;

        i += firstToken.length();
//        final int length = sql.length();
//        for (; i < length; i++) {
//            if (lookAhead(endToken, sql, i)) {
//                i += lookAheadLength(endToken); // look ahead 1
//                end = 1;
//                break;
//            }
//        }
        int endIndex = sql.indexOf(endToken, i);
        if (endIndex != -1) {
            i = endIndex + lookAheadLength(endToken);
            end = 1;
        } else {
            i = sql.length();
        }

        if (normalized != null) {
            normalized.append(sql, startIndex, i + end);
        }
        return i;
    }

    private static int lookAheadLength(String token) {
        return token.length() - 1;

    }

    private static boolean lookAhead(String token, String sql, int i) {
        return sql.startsWith(token, i);
    }

    private int lookAhead1(int index) {
        return lookAhead1(this.sql, index);
    }

    public static int lookAhead1(String sql, int index) {
        index++;
        if (index < sql.length()) {
            return sql.charAt(index);
        } else {
            return NEXT_TOKEN_NOT_EXIST;
        }
    }

    public static boolean lookAhead1(String sql, int index, char token) {
        return lookAhead1(sql, index) == token;
    }
}
