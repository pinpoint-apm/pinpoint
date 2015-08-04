package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 */
interface ParsingResultInternal extends ParsingResult {


    String getOriginalSql();

    boolean setId(int id);

    void setSql(String sql);

    void setOutput(String output);

}
