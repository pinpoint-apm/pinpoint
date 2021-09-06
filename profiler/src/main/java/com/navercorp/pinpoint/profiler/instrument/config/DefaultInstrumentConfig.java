/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.config;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilableClassFilter;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collections;
import java.util.List;

public class DefaultInstrumentConfig implements InstrumentConfig {

    public static final String INSTRUMENT_ENGINE_ASM = "ASM";

    @Value("${profiler.instrument.engine}")
    private String profileInstrumentEngine = INSTRUMENT_ENGINE_ASM;
    @Value("${profiler.instrument.matcher.enable}")
    private boolean instrumentMatcherEnable = true;

    @Value("${profiler.interceptorregistry.size}")
    private int interceptorRegistrySize = 1024 * 8;

    private List<String> allowJdkClassNames = Collections.emptyList();

    @Value("${profiler.pinpoint.base-package}")
    private String pinpointBasePackage;
    @Value("${profiler.pinpoint.exclude-package}")
    private String pinpointExcludePackage;

    private int callStackMaxDepth = 64;

    private Filter<String> profilableClassFilter = new SkipFilter<>();

    // service type
    @Value("${profiler.applicationservertype}")
    private String applicationServerType = null;

    @Value("${" + DefaultProfilerConfig.PROFILER_INTERCEPTOR_EXCEPTION_PROPAGATE + "}")
    private boolean propagateInterceptorException = false;

    @Value("${profiler.lambda.expressions.support}")
    private boolean supportLambdaExpressions = true;

    @Override
    public int getInterceptorRegistrySize() {
        return interceptorRegistrySize;
    }

    @Override
    public List<String> getAllowJdkClassName() {
        return allowJdkClassNames;
    }

    @Value("${profiler.instrument.jdk.allow.classnames}")
    void setAllowJdkClassNames(String allowJdkClassNames) {
        this.allowJdkClassNames = StringUtils.tokenizeToStringList(allowJdkClassNames, ",");
    }

    @Override
    public String getPinpointBasePackage() {
        return pinpointBasePackage;
    }

    @Override
    public String getPinpointExcludeSubPackage() {
        return pinpointExcludePackage;
    }

    @Override
    public Filter<String> getProfilableClassFilter() {
        return profilableClassFilter;
    }

    @Value("${profiler.include}")
    void setProfilableClassFilter(String profilableClass) {
        // TODO have to remove
        // profile package included in order to test "call stack view".
        // this config must not be used in service environment because the size of  profiling information will get heavy.
        // We may need to change this configuration to regular expression.
        if (profilableClass != null && !profilableClass.isEmpty()) {
            this.profilableClassFilter = new ProfilableClassFilter(profilableClass);
        } else {
            this.profilableClassFilter = new SkipFilter<>();
        }
    }

    @Override
    public String getApplicationServerType() {
        return applicationServerType;
    }

    public void setApplicationServerType(String applicationServerType) {
        this.applicationServerType = applicationServerType;
    }

    @Override
    public int getCallStackMaxDepth() {
        return callStackMaxDepth;
    }

    @Value("${profiler.callstack.max.depth}")
    public void setCallStackMaxDepth(int callStackMaxDepth) {
        // CallStack
        if (callStackMaxDepth != -1 && callStackMaxDepth < 2) {
            callStackMaxDepth = 2;
        }
        this.callStackMaxDepth = callStackMaxDepth;
    }

    @Override
    public boolean isPropagateInterceptorException() {
        return propagateInterceptorException;
    }

    @Override
    public String getProfileInstrumentEngine() {
        return profileInstrumentEngine;
    }

    @Override
    public boolean isSupportLambdaExpressions() {
        return supportLambdaExpressions;
    }

    @Override
    public boolean isInstrumentMatcherEnable() {
        return instrumentMatcherEnable;
    }

    @Override
    public String toString() {
        return "DefaultInstrumentConfig{" +
                "profileInstrumentEngine='" + profileInstrumentEngine + '\'' +
                ", instrumentMatcherEnable=" + instrumentMatcherEnable +
                ", interceptorRegistrySize=" + interceptorRegistrySize +
                ", allowJdkClassNames=" + allowJdkClassNames +
                ", pinpointBasePackage='" + pinpointBasePackage + '\'' +
                ", pinpointExcludePackage='" + pinpointExcludePackage + '\'' +
                ", callStackMaxDepth=" + callStackMaxDepth +
                ", profilableClassFilter=" + profilableClassFilter +
                ", applicationServerType='" + applicationServerType + '\'' +
                ", propagateInterceptorException=" + propagateInterceptorException +
                ", supportLambdaExpressions=" + supportLambdaExpressions +
                '}';
    }
}
