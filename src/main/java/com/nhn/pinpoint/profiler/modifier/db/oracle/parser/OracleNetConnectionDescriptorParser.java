package com.nhn.pinpoint.profiler.modifier.db.oracle.parser;

/**
 * @author emeroad
 */
public class OracleNetConnectionDescriptorParser {

    private static String THIN = "jdbc:oracle:thin";
    private static String OCI = "jdbc:oracle:oci";

    private String url;
    private String normalizedUrl;

    private DriverType driverType;

    private OracleNetConnectionDescriptorTokenizer tokenizer;

    public OracleNetConnectionDescriptorParser(String url) {
        this.url = url;
        this.normalizedUrl = url.toLowerCase();
        this.tokenizer = new OracleNetConnectionDescriptorTokenizer(normalizedUrl);
    }

    public KeyValue parse() {
        // 드라이버 스펙을 좀더 확인하려면 아래 참조. 10g 용이므로 11g도 거의 커버 될듯.
        // http://docs.oracle.com/cd/B14117_01/java.101/b10979/urls.htm

        int position;
        if (normalizedUrl.startsWith(THIN)) {
            position = nextPosition(THIN);
            driverType = DriverType.THIN;
        } else if(normalizedUrl.startsWith(OCI)) {
            position = nextPosition(OCI);
            driverType = DriverType.OCI;
        } else {
            // 파싱할 대상이 아님
            throw new IllegalArgumentException("invalid oracle jdbc url. expected token:(" + THIN + " or " + OCI + ")");
        }

        // thin 문자열 스킵
        this.tokenizer.setPosition(position);
        // token으로 분리.
        this.tokenizer.parse();
        // 구분분석.
        KeyValue keyValue = parseKeyValue();

        checkEof();

        return keyValue;
    }

    private void checkEof() {
        Token eof = this.tokenizer.nextToken();
        if (eof == null) {
            throw new OracleConnectionStringException("parsing error. expected token:'EOF' token:null");
        }
        if (eof != OracleNetConnectionDescriptorTokenizer.TOKEN_EOF_OBJECT) {
            throw new OracleConnectionStringException("parsing error. expected token:'EOF' token:" + eof);
        }
    }

    public DriverType getDriverType() {
        return driverType;
    }

    private int nextPosition(String driverUrl) {
        final int thinLength = driverUrl.length();
        if (normalizedUrl.startsWith(":@", thinLength)) {
            return thinLength + 2;
        } else if(normalizedUrl.startsWith("@", thinLength)) {
            return thinLength + 1;
        } else {
            throw new OracleConnectionStringException("invalid oracle jdbc url:" + driverUrl);
        }
    }

    private KeyValue parseKeyValue() {

        // start
        this.tokenizer.checkStartToken();

        KeyValue keyValue = new KeyValue();
        // key
        Token literalToken = this.tokenizer.getLiteralToken();
        keyValue.setKey(literalToken.getToken());

        // =
        this.tokenizer.checkEqualToken();

        // value 비교 reduce
        boolean nonTerminalValue = false;
        while(true) {
            final Token token = this.tokenizer.lookAheadToken();
            if (token == null) {
                // EOF하고는 다른 비정상적 종료인것으로 판단됨.
                throw new OracleConnectionStringException("Syntax error. lookAheadToken is null");
            }
            if (token.getType() == OracleNetConnectionDescriptorTokenizer.TYPE_KEY_START) {
                nonTerminalValue = true;
                KeyValue child = parseKeyValue();
                keyValue.addKeyValueList(child);

                // 다음 토큰을 더 까보고 )면 value 완성으로 종료.
                Token endCheck = this.tokenizer.lookAheadToken();
                if (endCheck == OracleNetConnectionDescriptorTokenizer.TOKEN_KEY_END_OBJECT) {
                    this.tokenizer.nextPosition();
                    return keyValue;
                }
            } else if(token.getType() == OracleNetConnectionDescriptorTokenizer.TYPE_LITERAL) {
                if (nonTerminalValue) {
                    throw new OracleConnectionStringException("Syntax error. expected token:'(' or ')' :" + token.getToken());
                }
                // lookahead로 봤으므로 토큰 버림.
                this.tokenizer.nextPosition();

                keyValue.setValue(token.getToken());
                this.tokenizer.checkEndToken();
                return keyValue;
            } else if(token.getType() == OracleNetConnectionDescriptorTokenizer.TYPE_KEY_END){
                this.tokenizer.nextPosition();
                // 빈칸이면 가능할듯 한데 뭔가 예외를 둬야 될듯하다.
                // empty value가 가능한가??
                return keyValue;
            } else {
                // START, END, LITERAL 을 다 체크하므로 불가능한거 같긴한데.
                // 향후 추가 토큰이 발생하면 에러 발생이 가능함.
                // 문법이 잘못됬을 경우 EOF가 오거나 할 수있음.
                throw new OracleConnectionStringException("Syntax error. " + token.getToken());
            }
        }

    }

}
