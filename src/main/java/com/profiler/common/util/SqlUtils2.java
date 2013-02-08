package com.profiler.common.util;

/**
 *
 */
@Deprecated
public class SqlUtils2 {
    public static final char SYMBOL_REPLACE = '$';

        private static int lookAhead(String sql, int index) {
        index++;
        if (index < sql.length()) {
            return sql.charAt(index);
        } else {
            return -1;
        }
    }

    public static final String normalizedSql0(String sql, StringBuilder outputParam, boolean flag) {
        if (sql == null) {
            return "";
        }
        int length = sql.length();
        StringBuilder normalized = new StringBuilder(length);

        boolean stringState = false;
        boolean flag2 = false;
        boolean charToken = false;
        boolean commentState = false;

        for (int i = 0; i < length; i++) {
            if (commentState) {

            }
            char c = sql.charAt(i);
            switch (c) {
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
                    if (commentState) {
                        normalized.append(c);
                        break;
                    }
                    if (stringState) {
                        outputParam.append(c);
                        break;
                    }
                    if (flag) {
                        if (!flag2) {
                            normalized.append('#');
                            flag2 = true;
                            outputParam.append(c);
                        } else {
                            outputParam.append(c);
                        }
                        break;
                    }
                    if (!flag2 && !charToken) {
                        normalized.append('#');
                        flag2 = true;
                        outputParam.append(c);
                        break;
                    }
                    if (charToken) {
                        normalized.append(c);
                    } else {
                        outputParam.append(c);
                    }
                    break;
                //Symbol
                case 39: // '\''
                    if (commentState) {
                        normalized.append(c);
                        break;
                    }
                    if (stringState) {
                        normalized.append('$').append('\'');
                        outputParam.append(',');
                    } else {
                        normalized.append('\'');
                    }
                    stringState = !stringState;
                    if (charToken)
                        charToken = false;
                    break;

                // 공백 space를 만남
                case '\t':
                case ' ':
                    if (commentState) {
                        normalized.append(c);
                        break;
                    }
                    if (stringState) {
                        outputParam.append(c);
                        break;
                    }
                    normalized.append(c);
                    if (flag2) {
                        flag2 = false;
                        outputParam.append(',');
                    }
                    if (charToken) {
                        charToken = false;
                    }
                    break;

                case 46: // '.'
                case 69: // 'E'
                    if (commentState) {
                        normalized.append(c);
                        break;
                    }
                    if (stringState || flag2) {
                        outputParam.append(c);
                        break;
                    }
                    normalized.append(c);
                    if (!charToken)
                        charToken = true;
                    break;

                case 45: // '-'
                    if (commentState) {
                        normalized.append(c);
                        break;
                    }
                    if (stringState || flag2) {
                        outputParam.append(c);
                        break;
                    }
                    if (i + 1 < length && sql.charAt(i + 1) == '-') {
                        int j = sql.indexOf('\n', i);
                        if (j > -1) {
                            normalized.append(sql.substring(i, j + 1));
                            i += j - i;
                        } else {
                            normalized.append(sql.substring(i));
                            i = length;
                        }
                    } else {
                        normalized.append(c);
                    }
                    if (charToken)
                        charToken = false;
                    break;

                case 10: // '\n'
                case 13: // '\r'
                    if (!stringState)
                        normalized.append(c);
                    if (charToken)
                        charToken = false;
                    if (flag2) {
                        outputParam.append(',');
                        flag2 = false;
                    }
                    break;

                case '/':
                    if (stringState) {
                        outputParam.append(c);
                        break;
                    }
                    if (lookAhead(sql, i) == '*' ) {
                        commentState = true;
                        normalized.append("/*");
                        i++;
                    } else {
                        normalized.append(c);
                    }
                    if (charToken)
                    charToken = false;
                    if (flag2)
                        flag2 = false;
                    break;

                case '*':
                    if (stringState) {
                        outputParam.append(c);
                        break;
                    }
                    if (i + 1 < length && sql.charAt(i + 1) == '/') {
                        normalized.append("*/");
                        commentState = false;
                        i++;
                    } else {
                        normalized.append(c);
                    }
                    if (charToken)
                        charToken = false;
                    if (flag2)
                        flag2 = false;
                    break;

                case 11: // '\013'
                case 12: // '\f'
                case 14: // '\016'
                case 15: // '\017'
                case 16: // '\020'
                case 17: // '\021'
                case 18: // '\022'
                case 19: // '\023'
                case 20: // '\024'
                case 21: // '\025'
                case 22: // '\026'
                case 23: // '\027'
                case 24: // '\030'
                case 25: // '\031'
                case 26: // '\032'
                case 27: // '\033'
                case 28: // '\034'
                case 29: // '\035'
                case 30: // '\036'
                case 31: // '\037'
                case 33: // '!'
                case 34: // '"'
                case 35: // '#'
                case 36: // '$'
                case 37: // '%'
                case 38: // '&'
                case 40: // '('
                case 41: // ')'
                case 43: // '+'
                case 44: // ','
                case 58: // ':'
                case 59: // ';'
                case 60: // '<'
                case 61: // '='
                case 62: // '>'
                case 63: // '?'
                case 64: // '@'
                case 65: // 'A'
                case 66: // 'B'
                case 67: // 'C'
                case 68: // 'D'
                default:
                    if (commentState) {
                        normalized.append(c);
                        break;
                    }
                    if (stringState) {
                        outputParam.append(c);
                        break;
                    }
                    if (flag2) {
                        normalized.append(c);
                        flag2 = false;
                        outputParam.append(',');
                    } else {
                        normalized.append(c);
                    }
                    if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_')
                        charToken = true;
                    else
                        charToken = false;
                    break;
            }
        }

        return normalized.toString();
    }


}
