package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.AntPathMatcher;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author emeroad
 */
// TODO development class
public class RpcURLPatternFilter implements URLPatternFilter {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final String urlPattern;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public RpcURLPatternFilter(String urlPattern) {
        if (urlPattern == null) {
            throw new NullPointerException("urlPattern must not be null");
        }
        // TODO remove decode
        this.urlPattern = new String(Base64.decodeBase64(urlPattern), UTF8);
        // TODO serviceType rpctype
    }

    @Override
    public boolean accept(List<SpanBo> fromSpanList) {
        for (SpanBo spanBo : fromSpanList) {
            List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
            if (spanEventBoList == null) {
                return REJECT;
            }

            for (SpanEventBo event : spanEventBoList) {
                if (!event.getServiceType().isRpcClient()) {
                    continue;
                }
                if (!event.getServiceType().isRecordStatistics()) {
                    continue;
                }
//                http://api.domain.com/test/ArticleList.do
//                slice url ->/test/ArticleList.do
                final List<AnnotationBo> annotationBoList = event.getAnnotationBoList();
                if (annotationBoList == null) {
                    continue;
                }
                for (AnnotationBo annotationBo : annotationBoList) {
//                    TODO ?? url format & annotation type detect
                    int key = annotationBo.getKey();
                    if (key == AnnotationKey.HTTP_URL.getCode() || key == AnnotationKey.NPC_URL.getCode()) {
                        String url = (String) annotationBo.getValue();
                        String path = getPath(url);
                        final boolean match = matcher.match(urlPattern, path);
                        if (match) {
                            return ACCEPT;
                        }
                    }
                }

            }
        }

        return REJECT;
    }

    private String getPath(String endPoint) {
        if (endPoint == null) {
            return  null;
        }
        // is URI format
        final int authorityIndex = endPoint.indexOf("://");
        if (authorityIndex == -1) {
            return endPoint;
        }
        final int pathIndex = endPoint.indexOf("/", authorityIndex + 1);
        if (pathIndex == -1) {
//            ???
            return endPoint;
        }
        return endPoint.substring(pathIndex+1);
    }
}
