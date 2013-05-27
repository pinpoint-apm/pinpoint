package com.profiler.modifier.db.oracle;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OracleConnectionStringTokenizer {

    public static final char TOKEN_EQUAL = '=';
    public static final char TOKEN_KEY_START = '(';
    public static final char TOKEN_KEY_END = ')';

    public static final int TYPE_KEY_START = 0;
    public static final Token TOKEN_KEY_START_OBJECT = new Token(String.valueOf(TOKEN_KEY_START), TYPE_KEY_START);

    public static final int TYPE_KEY_END = 1;
    public static final Token TOKEN_KEY_END_OBJECT = new Token(String.valueOf(TOKEN_KEY_END), TYPE_KEY_END);

    public static final int TYPE_EQUAL = 2;
    public static final Token TOKEN_EQUAL_OBJECT = new Token(String.valueOf(TOKEN_EQUAL), TYPE_EQUAL);

    public static final int TYPE_LITERAL = 3;

    private final List<Token> tokenList = new ArrayList<Token>();
    private int tokenPosition = 0;

    private final String connectionString;
    private int position = 0;

    public OracleConnectionStringTokenizer(String connectionString) {
        if (connectionString == null) {
            throw new NullPointerException("connectionString");
        }
        this.connectionString = connectionString;
    }

    public void parse() {
        final int length = connectionString.length();

        for (; position < length; position++) {
            final char ch = connectionString.charAt(position);
            if (isWhiteSpace(ch)) {
                continue;
            }

            switch (ch) {
                case TOKEN_KEY_START:
                    this.tokenList.add(TOKEN_KEY_START_OBJECT);
                    break;
                case TOKEN_EQUAL:
                    this.tokenList.add(TOKEN_EQUAL_OBJECT);
                    break;
                case TOKEN_KEY_END:
                    this.tokenList.add(TOKEN_KEY_END_OBJECT);
                    break;
                default:
                    String literal = parseLiteral();
                    addToken(literal, TYPE_LITERAL);
            }
        }
        return ;
    }

    String parseLiteral() {
        // literal 의 왼쪽 빈칸을 감는다.
        int start = trimLeft();

        for (position = start; position < connectionString.length(); position++) {
            final char ch = connectionString.charAt(position);
            switch (ch) {
                case TOKEN_EQUAL:
                case TOKEN_KEY_START:
                case TOKEN_KEY_END:
                    // literal 의 오른쪽 빈칸을 감는다.
                    int end = trimRight(position);
                    position--;
                    return connectionString.substring(start, end);
                default:
            }
        }
        // 문자열끝까지 옴.
        int end = trimRight(position);
        return connectionString.substring(start, end);
    }

    int trimRight(int index) {
        int end = index;
        for (; end > 0 ; end--) {
            final char ch = connectionString.charAt(end-1);
            if (!isWhiteSpace(ch)) {
                return end;
            }
        }
        return end;
    }

    int trimLeft() {
        final int length = connectionString.length();
        int start = position;
        for (; start < length; start++) {
            final char ch = connectionString.charAt(start);
            if (!isWhiteSpace(ch)) {
                return start;
            }
        }
        return start;
    }

    private void addToken(String tokenString, int type) {
        Token token = new Token(tokenString, type);
        this.tokenList.add(token);
    }


    private boolean isWhiteSpace(char ch) {
        return (ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r');
    }


    public Token nextToken() {
        if (tokenList.size() <= tokenPosition) {
            return null;
        }
        Token token = tokenList.get(tokenPosition);
        tokenPosition++;
        return token;
    }

    public void nextPosition() {
        if (tokenList.size() <= tokenPosition) {
            return;
        }
        tokenPosition++;
    }

    public Token lookAheadToken() {
        if (tokenList.size() <= tokenPosition) {
            return null;
        }
        return tokenList.get(tokenPosition);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void checkStartToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token == TOKEN_KEY_START_OBJECT)) {
            throw new OracleConnectionStringException("syntax error. Expected token='(' :" + token.getToken());
        }
    }

    public void checkEqualToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token == TOKEN_EQUAL_OBJECT)) {
            throw new OracleConnectionStringException("syntax error. Expected token='=' :" + token.getToken());
        }
        return ;
    }

    public void checkEndToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token == TOKEN_KEY_END_OBJECT)) {
            throw new OracleConnectionStringException("syntax error. Expected token=')' :" + token.getToken());
        }
        return;
    }

    public Token getLiteralToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token.getType() == TYPE_LITERAL)) {
            throw new OracleConnectionStringException("syntax error. Expected token=( :" + token.getToken());
        }
        return token;
    }

    public Token getLiteralToken(String expectedValue) {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token.getType() == TYPE_LITERAL)) {
            throw new OracleConnectionStringException("syntax error. Expected token=( :" + token.getToken());
        }
        if (!expectedValue.equals(token.getToken())) {
            throw new OracleConnectionStringException("syntax error. Expected token=" + expectedValue + "' :" + token.getToken());
        }
        return token;
    }
}
