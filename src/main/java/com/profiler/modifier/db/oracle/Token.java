package com.profiler.modifier.db.oracle;

/**
 *
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
}
