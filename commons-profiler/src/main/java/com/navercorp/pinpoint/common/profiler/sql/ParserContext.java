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
    //    private final StringBuilder parsedParameter;
    private final ParameterBuilder parameter;

    private final boolean removeComments;

    private int replaceIndex = 0;

    public ParserContext(String sql, boolean removeComments) {
        this.sql = Objects.requireNonNull(sql, "sql");
        this.length = sql.length();
        this.removeComments = removeComments;

        this.normalized = new StringBuilder(length + NORMALIZED_SQL_BUFFER);
//        this.parsedParameter = new StringBuilder(32);
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
//                case '#'
//                    # is a single line comment in mysql
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
                        i = readNumber(ch, i);
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
//                                    outputParam.append(',');
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

    private int readComment(String first, int i, StringBuilder normalized) {
        return readComment0(first, i, normalized, false);
    }

    private int skipComment(String first, int i) {
        return readComment0(first, i, null, true);
    }

    private int readComment0(String first, int i, StringBuilder normalized, boolean skip) {
        final int startIndex = i;
        int end = 0;

        i += first.length();
        for (; i < this.length; i++) {
            if (sql.charAt(i) == '*' && lookAhead1(i, '/')) {
                i++; // look ahead 1
                end = foundLookAhead("*/");
                break;
            }
        }

        if (!skip) {
            normalized.append(sql, startIndex, i + end);
        }
        return i;
    }


    private int readNumber(char first, int i) {
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
//                                    outputParam.append(SEPARATOR);
                    end = 1;
                    i--;
                    break tokenEnd;
            }
        }

        parameter.append(sql, startIndex, i + end);
        return i;
    }

    private int skipLine(String first, int i, StringBuilder normalized) {
        int end = 0;

        i += first.length();
        for (; i < this.length; i++) {
            if (sql.charAt(i) == '\n') {
                end = 1; // token end
                break;
            }
        }

        if (end == 1) {
            normalized.append('\n');
        }
        return i;
    }

    private int readLine(String first, int index, StringBuilder normalized) {
        return readLine(first, index, this.sql, normalized);
    }

    public static int readLine(String first, int i, String sql, StringBuilder normalized) {
        final int startIndex = i;
        int end = 0;

        i += first.length();
        final int length = sql.length();
        for (; i < length; i++) {
            if (sql.charAt(i) == '\n') {
                end =  1; // token end
                break;
            }
        }

        normalized.append(sql, startIndex, i + end);
        return i;
    }

    public static int readMultiLineComment(String first, int i, String sql, StringBuilder normalized) {
        final int startIndex = i;
        int end = 0;

        i += first.length();
        final int length = sql.length();
        for (; i < length; i++) {
            if (sql.charAt(i) == '*' && lookAhead1(sql, i) == '/') {
                i++; // look ahead 1
                end = foundLookAhead("*/");
                break;
            }
        }

        normalized.append(sql, startIndex, i + end);
        return i;
    }

    private static int foundLookAhead(String token) {
        return token.length() - 1;
    }

    /**
     * look up the next character in a string
     *
     */
    private int lookAhead1(int index) {
        return lookAhead1(this.sql, index);
    }

    /**
     * look up the next character in a string
     *
     * @param index
     */
    public static int lookAhead1(String sql, int index) {
        index++;
        if (index < sql.length()) {
            return sql.charAt(index);
        } else {
            return NEXT_TOKEN_NOT_EXIST;
        }
    }

    private boolean lookAhead1(int index, char token) {
        return lookAhead1(index) == token;
    }

    public static boolean lookAhead1(String sql, int index, char token) {
        return lookAhead1(sql, index) == token;
    }
}
