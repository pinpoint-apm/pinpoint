package com.nhn.pinpoint.plugin.arcus;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.ParameterExtractor;
import com.nhn.pinpoint.bootstrap.plugin.ParameterExtractorFactory;

public abstract class Commons {
    private Commons() {}
    
    public static final String ARCUS_SCOPE = "ArcusScope";
    
    public static final ParameterExtractorFactory ARCUS_KEY_EXTRACTOR_FACTORY = new ParameterExtractorFactory() {
        
        @Override
        public ParameterExtractor get(InstrumentClass targetClass, MethodInfo targetMethod) {
            final int index = ParameterUtils.findFirstString(targetMethod, 3);
            
            if (index != -1) {
                return new IndexParameterExtractor(index);
            }
            
            return null;
        }
    };
}
