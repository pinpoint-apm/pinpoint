package com.navercorp.pinpoint.bootstrap.transaction;

import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.bootstrap.util.AntPathMatcher;

import java.util.HashSet;
import java.util.Set;

public class RequestMappingInfo implements IRequestMappingInfo {

    private AntPathMatcher antPathMatcher;
    private Set<String> methods;
    private String pattern;
    private String methodsStr;

    public RequestMappingInfo(String pattern, Set<String> methods) {

        this.antPathMatcher = new AntPathMatcher(pattern);
        this.methods = methods;
        this.pattern = pattern;
        StringBuilder sb = new StringBuilder();
        for (String method : methods) {
            sb.append(method)
                    .append(",");
        }
        sb.append("_");

        this.methodsStr = sb.toString().replaceAll(",_", "_");
    }

    public RequestMappingInfo(String pattern, String... methods) {
        Set<String> methodSet = new HashSet<String>();
        for (String method : methods) {
            methodSet.add(method);
        }
        this.antPathMatcher = new AntPathMatcher(pattern);
        this.methods = methodSet;
        this.pattern = pattern;
    }

    public boolean match(String uri, String method) {
        return (methods.contains("ALL") || methods.contains(method)) && antPathMatcher.isMatched(uri);
    }

    public String getTransactionType() {
        return methodsStr + pattern;
    }

    @Override
    public String toString() {
        return "RequestMappingInfo{" +
                "pattern=" + pattern +
                ", methods=" + methods +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestMappingInfo that = (RequestMappingInfo) o;

        if (methods != null ? !methods.equals(that.methods) : that.methods != null) return false;
        return pattern != null ? pattern.equals(that.pattern) : that.pattern == null;
    }

    @Override
    public int hashCode() {
        int result = methods != null ? methods.hashCode() : 0;
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        return result;
    }
}
