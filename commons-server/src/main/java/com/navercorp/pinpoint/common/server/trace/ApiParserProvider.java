package com.navercorp.pinpoint.common.server.trace;

public class ApiParserProvider {

    private final ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();
    private final ApiParser parser = new DefaultApiParser(apiDescriptionParser);


    public ApiParserProvider() {
    }

    public ApiParser getParser() {
        return parser;
    }
}
