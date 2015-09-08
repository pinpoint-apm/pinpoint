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
        // You can find driver spec here: http://docs.oracle.com/cd/B14117_01/java.101/b10979/urls.htm
        // It's for 10g but maybe 11g would be same.
        
        int position;
        if (normalizedUrl.startsWith(THIN)) {
            position = nextPosition(THIN);
            driverType = DriverType.THIN;
        } else if(normalizedUrl.startsWith(OCI)) {
            position = nextPosition(OCI);
            driverType = DriverType.OCI;
        } else {
            throw new IllegalArgumentException("invalid oracle jdbc url. expected token:(" + THIN + " or " + OCI + ") url:" + url);
        }

        // skip thin string
        this.tokenizer.setPosition(position);

        this.tokenizer.parse();
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

        // value compare reduce
        boolean nonTerminalValue = false;
        while(true) {
            final Token token = this.tokenizer.lookAheadToken();
            if (token == null) {
                // Abnormal termination.
                throw new OracleConnectionStringException("Syntax error. lookAheadToken is null");
            }
            if (token.getType() == OracleNetConnectionDescriptorTokenizer.TYPE_KEY_START) {
                nonTerminalValue = true;
                KeyValue child = parseKeyValue();
                keyValue.addKeyValueList(child);

                // if next token is ')', value is completed.
                Token endCheck = this.tokenizer.lookAheadToken();
                if (endCheck == OracleNetConnectionDescriptorTokenizer.TOKEN_KEY_END_OBJECT) {
                    this.tokenizer.nextPosition();
                    return keyValue;
                }
            } else if(token.getType() == OracleNetConnectionDescriptorTokenizer.TYPE_LITERAL) {
                if (nonTerminalValue) {
                    throw new OracleConnectionStringException("Syntax error. expected token:'(' or ')' :" + token.getToken());
                }
                // We already have checked current token by lookAheadToken(). Proceed to next token.
                this.tokenizer.nextPosition();

                keyValue.setValue(token.getToken());
                this.tokenizer.checkEndToken();
                return keyValue;
            } else if(token.getType() == OracleNetConnectionDescriptorTokenizer.TYPE_KEY_END){
                this.tokenizer.nextPosition();
                // This could happen if value is empty.
                // Does it allow empty value?
                return keyValue;
            } else {
                // Cannot reach here because we checked all those possible cases, START, END and LITERAL.
                // Adding new token type could cause error.
                // In case of syntax error, EOF can come to here. 
                throw new OracleConnectionStringException("Syntax error. " + token.getToken());
            }
        }

    }

}
