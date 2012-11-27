package com.profiler.common.mapping.code;

import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.mapping.ClassMapping;
import com.profiler.common.mapping.MethodMapping;
import com.profiler.common.mapping.Register;

/**
 *
 */
public class RpcClientRegister implements Register {

    public static final int ApacheHttpClient4Code = 10000;
    private static final ClassMapping ApacheHttpClient4 = new ClassMapping(ApacheHttpClient4Code, "org.apache.http.impl.client.AbstractHttpClient",
            new MethodMapping("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest"}, new String[]{"request"}),
            new MethodMapping("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"},
                    new String[]{"target", "request", "responseHandler", "context"})
    );

    @Override
    public void register(ApiMappingTable apiMappingTable, int startRange, int endRange) {
        apiMappingTable.put(ApacheHttpClient4);
    }
}
