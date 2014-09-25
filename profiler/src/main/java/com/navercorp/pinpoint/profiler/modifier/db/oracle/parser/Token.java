package com.nhn.pinpoint.profiler.modifier.db.oracle.parser;

/**
 * @author emeroad
 */
public class Token {

    private String token;
    private int type;

    public Token(String token, int type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Token");
        sb.append("{token='").append(token).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
