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

package com.navercorp.pinpoint.plugin.jdbc.oracle.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class OracleNetConnectionDescriptorTokenizer {

    public static final char TOKEN_EQUAL = '=';
    public static final char TOKEN_KEY_START = '(';
    public static final char TOKEN_KEY_END = ')';

    // Connection methodDescriptor can contain below tokens too.
    // But we don't support them right now.
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
                    // TODO handle these tokens.
                    // Need to study how these tokens are used.
                    throw new OracleConnectionStringException("unsupported token:" + ch);
                default:
                    String literal = parseLiteral();
                    addToken(literal, TYPE_LITERAL);
            }
        }
        this.tokenList.add(TOKEN_EOF_OBJECT);
    }

    String parseLiteral() {
        int start = trimLeft();

        for (position = start; position < connectionString.length(); position++) {
            final char ch = connectionString.charAt(position);
            switch (ch) {
                case TOKEN_EQUAL:
                case TOKEN_KEY_START:
                case TOKEN_KEY_END:
                    int end = trimRight(position);
                    
                    // step back position because last seen character is not part of this literal.
                    position--;
                    return connectionString.substring(start, end);
                default:
            }
        }
        // end of the string.
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
        // We can check by == because the token object is singleton.
        if (!(token == TOKEN_KEY_START_OBJECT)) {
            throw new OracleConnectionStringException("syntax error. Expected token='(' :" + token.getToken());
        }
    }

    public void checkEqualToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null. Expected token='='");
        }
        // We can check by == because the token object is singleton.
        if (!(token == TOKEN_EQUAL_OBJECT)) {
            throw new OracleConnectionStringException("Syntax error. Expected token='=' :" + token.getToken());
        }
    }

    public void checkEndToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null. Expected token=')");
        }
        // We can check by == because the token object is singleton.
        if (!(token == TOKEN_KEY_END_OBJECT)) {
            throw new OracleConnectionStringException("Syntax error. Expected token=')' :" + token.getToken());
        }
    }

    public Token getLiteralToken() {
        Token token = this.nextToken();
        if (token == null) {
            throw new OracleConnectionStringException("parse error. token is null. Expected token='LITERAL'");
        }
        // We can check by == because the token object is singleton.
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
        // We can check by == because the token object is singleton.
        if (!(token.getType() == TYPE_LITERAL)) {
            throw new OracleConnectionStringException("Syntax error. Expected token='LITERAL' :" + token.getToken());
        }
        if (!expectedValue.equals(token.getToken())) {
            throw new OracleConnectionStringException("Syntax error. Expected token=" + expectedValue + "' :" + token.getToken());
        }
        return token;
    }
}
