package com.nhn.pinpoint.profiler.modifier.db.oracle.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class OracleNetConnectionDescriptorTokenizer {

    public static final char TOKEN_EQUAL = '=';
    public static final char TOKEN_KEY_START = '(';
    public static final char TOKEN_KEY_END = ')';

//    사실 아래 토큰이 더 있음 추후 이슈 발생시 추가로 구현이 필요함.
//    현재는 그냥 지원하지 않는다고 에러나 발생시키자.
    private static final char TOKEN_COMMA = ',';
    private static final char TOKEN_BKSLASH = '\\';
    private static final char TOKEN_DQUOTE = '"';
    private static final char TOKEN_SQUOTE = '\'';

    public static final int TYPE_KEY_START = 0;
    public static final Token TOKEN_KEY_START_OBJECT = new Token(String.valueOf(TOKEN_KEY_START), TYPE_KEY_START);

    public static final int TYPE_KEY_END = 1;
    public static final Token TOKEN_KEY_END_OBJECT = new Token(String.valueOf(TOKEN_KEY_END), TYPE_KEY_END);

    public static final int TYPE_EQUAL = 2;
    public static final Token TOKEN_EQUAL_OBJECT = new Token(String.valueOf(TOKEN_EQUAL), TYPE_EQUAL);

    public static final int TYPE_LITERAL = 3;

    public static final int TYPE_EOF = -1;
    public static final Token TOKEN_EOF_OBJECT = new Token("EOF", TYPE_EOF);

    private final List<Token> tokenList = new ArrayList<Token>();
    private int tokenPosition = 0;

    private final String connectionString;
    private int position = 0;

    public OracleNetConnectionDescriptorTokenizer(String connectionString) {
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
                case TOKEN_COMMA:
                case TOKEN_BKSLASH:
                case TOKEN_DQUOTE:
                case TOKEN_SQUOTE:
                    // TODO 위 4개 토큰에 대해서 추가 구현이 필요함.
                    // 사내에서는 안쓰고 어떻게 구문이 완성되는건지 모르니 일단 skip하자.
                    throw new OracleConnectionStringException("unsupported token:" + ch);
                default:
                    String literal = parseLiteral();
                    addToken(literal, TYPE_LITERAL);
            }
        }
        this.tokenList.add(TOKEN_EOF_OBJECT);
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
                    // 마지막 토큰을 본것은 다시 리셋해야 되므로 pos를 역으로 치환.
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
            throw new OracleConnectionStringException("parse error. token is null. Expected token='='");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token == TOKEN_EQUAL_OBJECT)) {
            throw new OracleConnectionStringException("Syntax error. Expected token='=' :" + token.getToken());
        }
        return ;
    }

    public void checkEndToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null. Expected token=')");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token == TOKEN_KEY_END_OBJECT)) {
            throw new OracleConnectionStringException("Syntax error. Expected token=')' :" + token.getToken());
        }
        return;
    }

    public Token getLiteralToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null. Expected token='LITERAL'");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token.getType() == TYPE_LITERAL)) {
            throw new OracleConnectionStringException("Syntax error. Expected token='LITERAL'' :" + token.getToken());
        }
        return token;
    }

    public Token getLiteralToken(String expectedValue) {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null. Expected token='LITERAL'");
        }
        // 객체를 재활용한므로 == 해도됨
        if (!(token.getType() == TYPE_LITERAL)) {
            throw new OracleConnectionStringException("Syntax error. Expected token='LITERAL' :" + token.getToken());
        }
        if (!expectedValue.equals(token.getToken())) {
            throw new OracleConnectionStringException("Syntax error. Expected token=" + expectedValue + "' :" + token.getToken());
        }
        return token;
    }
}
