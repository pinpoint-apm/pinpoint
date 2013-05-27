package com.profiler.modifier.db.oracle;

/**
 *
 */
public class OracleURLParser {

    private static final char TOKEN_EQUAL = '=';
    private static final String EQUAL = String.valueOf(TOKEN_EQUAL);
    private static final char TOKEN_KEY_START = '(';
    private static final String KEY_START = String.valueOf(TOKEN_KEY_START);
    private static final char TOKEN_KEY_END = ')';
    private static final String KEY_END = String.valueOf(TOKEN_KEY_END);

    private static String THIN = "jdbc:oracle:thin";

    private String url;
    private String normalizedUrl;
    private int position = 0;

    private OracleConnectionStringTokenizer tokenizer;

    public OracleURLParser(String url) {
        this.url = url;
        this.normalizedUrl = url.toLowerCase();
        this.tokenizer = new OracleConnectionStringTokenizer(normalizedUrl);

    }

    public KeyValue parse() {
        if (!normalizedUrl.startsWith(THIN)) {
            // 파싱할 대상이 아님
            throw new IllegalArgumentException("invalid oracle jdbc url");
        }
        final int thinLength = THIN.length();
        if (normalizedUrl.startsWith(":@", thinLength)) {
            position = thinLength + 2;
        } else if(normalizedUrl.startsWith("@", thinLength)) {
            position = thinLength + 1;
        } else {
            throw new IllegalArgumentException("invalid oracle jdbc url");
        }
        // thin문자열 스킵
        this.tokenizer.setPosition(position);
        this.tokenizer.parse();

        KeyValue keyValue = readKeyValue();
        return keyValue;
    }

    private KeyValue readKeyValue() {

        // start
        this.tokenizer.checkStartToken();

        KeyValue keyValue = new KeyValue();
        // key
        Token literalToken = this.tokenizer.getLiteralToken();
        keyValue.setKey(literalToken.getToken());

        // =
        this.tokenizer.checkEqualToken();

        // value 비교
        while(true) {
            Token token = this.tokenizer.lookAheadToken();
            if (token == OracleConnectionStringTokenizer.TOKEN_KEY_END_OBJECT) {
                tokenizer.nextPosition();
                return keyValue;
            }
            if (token.getType() == OracleConnectionStringTokenizer.TYPE_KEY_START) {
                KeyValue child = readKeyValue();
                keyValue.addKeyValueList(child);
            } else {
                // lookahead로 봤으므로 토큰 버림.
                this.tokenizer.nextPosition();
                if (token.getType() == OracleConnectionStringTokenizer.TYPE_LITERAL) {
                    keyValue.setValue(token.getToken());
                    this.tokenizer.checkEndToken();
                    return keyValue;
                } if(token.getType() == OracleConnectionStringTokenizer.TYPE_KEY_END){
                    // 빈칸이면 가능할듯 한데 뭔가 예외를 둬야 될듯하다.
                    // 뒷 토큰이 계속 있는지 체크
                    Token last = tokenizer.lookAheadToken();
                    if(last == OracleConnectionStringTokenizer.TOKEN_KEY_END_OBJECT) {
                        tokenizer.nextPosition();
                        return keyValue;
                    }
                }
                else {
                    throw new OracleConnectionStringException("syntax error. Expected token='literal' :" + token.getToken());
                }
            }
        }
    }

}
