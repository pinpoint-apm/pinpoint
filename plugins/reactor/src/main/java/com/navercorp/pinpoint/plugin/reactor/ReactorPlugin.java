/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.ConnectableFluxSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.ConnectableFluxConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxDelaySubscriptionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxDelaySubscriptionSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxOperatorConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FuncationApplyInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.MonoConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.MonoDelaySubscriptionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.MonoDelaySubscriptionSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.ParallelFluxConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.ParallelFluxSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.ProcessorSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.MonoOperatorConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.MonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.MonoSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RunnableCoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RunnableCoreSubscriberInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoPublishOnInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class ReactorPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    private static final String[] PROCESSOR = {
            "reactor.core.publisher.MonoProcessor",
            "reactor.core.publisher.FluxProcessor",
            "reactor.core.publisher.UnicastProcessor",
            "reactor.core.publisher.DirectProcessor",
            "reactor.core.publisher.EmitterProcessor",
            "reactor.core.publisher.DelegateProcessor",
            "reactor.core.publisher.EventLoopProcessor",
            "reactor.core.publisher.WorkQueueProcessor",
            "reactor.core.publisher.TopicProcessor",
            "reactor.core.publisher.ReplayProcessor",
            "reactor.core.publisher.NextProcessor"
    };

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ReactorPluginConfig config = new ReactorPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} version range=[3.1.0.RELEASE, 3.3.0.RELEASE], config:{}", this.getClass().getSimpleName(), config);

        addFlux();
        addMono();
        addParallelFlux();
        addCoreSubscriber();
        addProcessor();
        addFunction();
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addFlux() {
        transformTemplate.transform("reactor.core.publisher.Flux", FluxMethodTransform.class);
        // publishOn
        transformTemplate.transform("reactor.core.publisher.FluxPublishOn", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPublishOn$PublishOnConditionalSubscriber", RunnableCoreSubscriberTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPublishOn$PublishOnSubscriber", RunnableCoreSubscriberTransform.class);
        // subscribeOn
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnValue", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnValue$ScheduledEmpty", RunnableCoreSubscriberTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnValue$ScheduledScalar", RunnableCoreSubscriberTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnCallable", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnCallable$CallableSubscribeOnSubscription", RunnableCoreSubscriberTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOn", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOn$SubscribeOnSubscriber", RunnableCoreSubscriberTransform.class);

        // Flux
        transformTemplate.transform("reactor.core.publisher.ConnectableFluxHide", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ConnectableFluxOnAssembly", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ConnectableLift", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ConnectableLiftFuseable", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxArray", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxAutoConnect", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxAutoConnectFuseable", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxBuffer", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxBufferBoundary", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxBufferPredicate", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxBufferTimeout", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxBufferWhen", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxCallable", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxCallableOnAssembly", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxCancelOn", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxCombineLatest", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxConcatArray", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxConcatIterable", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxConcatMap", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxConcatMapNoPrefetch", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxContextWrite", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxCreate", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDefaultIfEmpty", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDefer", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDeferContextual", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDelaySequence", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDelaySubscription", FluxDelaySubscriptionTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDematerialize", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDetach", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDistinct", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDistinctFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDistinctUntilChanged", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDoFinally", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDoFinallyFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDoFirst", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDoFirstFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDoOnEach", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxDoOnEachFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxElapsed", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxEmpty", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxError", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxErrorOnRequest", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxErrorSupplied", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxExpand", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFilter", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFilterFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFilterWhen", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFirstWithSignal", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFirstWithValue", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFlatMap", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFlattenIterable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxFromMonoOperator", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxGenerate", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxGroupBy", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxGroupJoin", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxHandle", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxHandleFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxHide", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxIndex", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxIndexFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxInterval", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxIterable", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxJoin", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxJust", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxLift", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxLiftFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxLimitRequest", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxLog", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxLogFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMap", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMapFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMapSignal", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMaterialize", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMerge", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMergeComparing", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMergeSequential", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMetrics", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMetricsFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxName", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxNameFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxNever", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnAssembly", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnBackpressureBuffer", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnBackpressureBufferStrategy", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnBackpressureBufferTimeout", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnBackpressureDrop", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnBackpressureLatest", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnErrorResume", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPeek", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPeekFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPublish", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPublishMulticast", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRange", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRefCount", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRefCountGrace", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRepeat", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRepeatPredicate", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRepeatWhen", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxReplay", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRetry", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRetryPredicate", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRetryWhen", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSample", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSampleFirst", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSampleTimeout", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxScan", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxScanSeed", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSkip", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSkipLast", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSkipUntil", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSkipWhile", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSource", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSourceFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSourceMono", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSourceMonoFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxStream", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSwitchIfEmpty", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSwitchMap", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSwitchMapNoPrefetch", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSwitchOnFirst", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTake", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTakeFuseable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTakeLast", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTakeLastOne", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTakeUntil", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTakeUntilOther", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTakeWhile", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTimed", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxTimeout", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxUsing", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxUsingWhen", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxWindow", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxWindowBoundary", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxWindowPredicate", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxWindowTimeout", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxWindowWhen", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxWithLatestFrom", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxZip", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxZipIterable", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.GroupedLift", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.GroupedLiftFuseable", FluxOperatorTransform.class);

        transformTemplate.transform("reactor.core.publisher.FluxFirstEmitting", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxMergeOrdered", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSkipUntilOther", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxContextStart", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxPublishOnCaptured", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSchedulerCapture", FluxOperatorTransform.class);

        transformTemplate.transform("reactor.core.publisher.ParallelMergeOrdered", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelMergeSequential", FluxOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelMergeSort", FluxOperatorTransform.class);

        transformTemplate.transform("reactor.core.publisher.SinkManyBestEffort", FluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.UnicastManySinkNoBackpressure", FluxTransform.class);

        transformTemplate.transform("reactor.core.publisher.InternalConnectableFluxOperator", ConnectableFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.InternalFluxOperator", FluxOperatorTransform.class);
    }

    private void addMono() {
        transformTemplate.transform("reactor.core.publisher.Mono", MonoMethodTransform.class);
        // publishOn
        transformTemplate.transform("reactor.core.publisher.MonoPublishOn", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoPublishOn$PublishOnSubscriber", RunnableCoreSubscriberTransform.class);
        // subscribeOn
        transformTemplate.transform("reactor.core.publisher.MonoSubscribeOnValue", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSubscribeOnCallable", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSubscribeOn", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSubscribeOn$SubscribeOnSubscriber", RunnableCoreSubscriberTransform.class);

        // mono
        transformTemplate.transform("reactor.core.publisher.MonoAll", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoAny", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCacheInvalidateIf", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCacheInvalidateWhen", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCallable", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCacheTime", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCancelOn", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCollect", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCollectList", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCompletionStage", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoContextWrite", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCount", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCreate", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoCurrentContext", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDefaultIfEmpty", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDefer", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDeferContextual", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDelay", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDelayElement", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDelaySubscription", MonoDelaySubscriptionTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDelayUntil", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDematerialize", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDetach", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDoFinally", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDoFinallyFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFirst", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDoFirstFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDoOnEach", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDoOnEachFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoElapsed", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoElementAt", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoEmpty", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoError", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoErrorSupplied", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoExpand", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFilter", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFilterFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFilterWhen", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFirstWithSignal", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFirstWithValue", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFlatMap", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFlatMapMany", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFlattenIterable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFromFluxOperator", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoFromPublisher", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoHandle", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoHandleFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoHasElement", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoHasElements", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoHide", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoIgnoreElement", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoIgnoreElements", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoIgnorePublisher", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoIgnoreThen", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoJust", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoLift", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoLiftFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoLog", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoLogFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoMap", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoMapFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoMaterialize", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoMetrics", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoMetricsFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoName", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoNameFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoNever", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoNext", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoOnAssembly", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoOnErrorResume", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoPeek", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoPeekFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoPeekTerminal", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoPublishMulticast", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoReduce", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoReduceSeed", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRepeat", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRepeatPredicate", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRepeatWhen", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRetry", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRetryPredicate", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRetryWhen", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoRunnable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSequenceEqual", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSingle", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSingleCallable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSingleMono", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSource", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSourceFlux", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSourceFluxFuseable", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSourceFuseable", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoStreamCollector", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSupplier", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoSwitchIfEmpty", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoTakeLastOne", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoTakeUntilOther", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoTimed", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoTimeout", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoUsing", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoUsingWhen", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoWhen", MonoTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoZip", MonoTransform.class);

        transformTemplate.transform("reactor.core.publisher.MonoSubscriberContext", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelMergeReduce", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelThen", MonoOperatorTransform.class);

        transformTemplate.transform("reactor.core.publisher.SinkEmptyMulticast", MonoOperatorTransform.class);
        transformTemplate.transform("reactor.core.publisher.InternalMonoOperator", MonoOperatorTransform.class);
    }

    private void addParallelFlux() {
        transformTemplate.transform("reactor.core.publisher.ParallelFlux", ParallelFluxMethodTransform.class);
        // runOn
        transformTemplate.transform("reactor.core.publisher.ParallelRunOn", ParallelFluxTransform.class);

        transformTemplate.transform("reactor.core.publisher.ParallelArraySource", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelCollect", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelConcatMap", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelDoOnEach", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelFilter", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelFlatMap", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelFluxHide", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelFluxName", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelFluxOnAssembly", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelGroup", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelLift", ParallelFluxTransform.class);

        transformTemplate.transform("reactor.core.publisher.ParallelLiftFuseable", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelLog", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelMap", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelPeek", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelReduceSeed", ParallelFluxTransform.class);
        transformTemplate.transform("reactor.core.publisher.ParallelSource", ParallelFluxTransform.class);
    }

    private void addCoreSubscriber() {
        final Matcher coreSubscriberMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(coreSubscriberMatcher, CoreSubscriberTransform.class);
    }

    private void addProcessor() {
        for (String className : PROCESSOR) {
            transformTemplate.transform(className, ProcessorTransform.class);
        }
    }

    private void addFunction() {
        final Matcher functionMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new InterfaceInternalNameMatcherOperand("java.util.function.Function", true));
        transformTemplate.transform(functionMatcher, FunctionTransform.class);
    }

    public static class FluxMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod publishOnMethod = target.getDeclaredMethod("publishOn", "reactor.core.scheduler.Scheduler", "boolean", "int", "int");
            if (publishOnMethod != null) {
                publishOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
            }
            final InstrumentMethod subscribeOnMethod = target.getDeclaredMethod("subscribeOn", "reactor.core.scheduler.Scheduler", "boolean");
            if (subscribeOnMethod != null) {
                subscribeOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class FluxTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(FluxConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }

            return target.toBytecode();
        }
    }

    public static class FluxOperatorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(FluxOperatorConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxOperatorSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxOperatorSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }

            return target.toBytecode();
        }
    }

    public static class FluxDelaySubscriptionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(FluxDelaySubscriptionConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            final InstrumentMethod acceptMethod = target.getDeclaredMethod("accept", "reactor.core.publisher.FluxDelaySubscription$DelaySubscriptionOtherSubscriber");
            if (acceptMethod != null) {
                acceptMethod.addInterceptor(FluxDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }

            return target.toBytecode();
        }
    }

    public static class RunnableCoreSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(RunnableCoreSubscriberConstructorInterceptor.class);
                }
            }
            final InstrumentMethod runMethod = target.getDeclaredMethod("run");
            if (runMethod != null) {
                runMethod.addInterceptor(RunnableCoreSubscriberInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MonoMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod publishOnMethod = target.getDeclaredMethod("publishOn", "reactor.core.scheduler.Scheduler");
            if (publishOnMethod != null) {
                publishOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
            }
            final InstrumentMethod subscribeOnMethod = target.getDeclaredMethod("subscribeOn", "reactor.core.scheduler.Scheduler");
            if (subscribeOnMethod != null) {
                subscribeOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class MonoTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(MonoConstructorInterceptor.class);
                }
            }
            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(MonoSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(MonoSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            return target.toBytecode();
        }
    }

    public static class MonoOperatorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(MonoOperatorConstructorInterceptor.class);
                }
            }
            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(MonoOperatorSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(MonoOperatorSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            return target.toBytecode();
        }
    }

    public static class MonoDelaySubscriptionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(MonoDelaySubscriptionConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(MonoDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(MonoDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            final InstrumentMethod acceptMethod = target.getDeclaredMethod("accept", "reactor.core.publisher.FluxDelaySubscription$DelaySubscriptionOtherSubscriber");
            if (acceptMethod != null) {
                acceptMethod.addInterceptor(MonoDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }

            return target.toBytecode();
        }
    }

    public static class ParallelFluxMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod runOnMethod = target.getDeclaredMethod("runOn", "reactor.core.scheduler.Scheduler", "int");
            if (runOnMethod != null) {
                runOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ParallelFluxTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(ParallelFluxConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber[]");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(ParallelFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            return target.toBytecode();
        }
    }

    public static class ProcessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(ProcessorSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }

            return target.toBytecode();
        }
    }

    public static class ConnectableFluxTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(ConnectableFluxConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(ConnectableFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(ConnectableFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }
            final InstrumentMethod connectMethod = target.getDeclaredMethod("connect", "java.util.function.Consumer");
            if (connectMethod != null) {
                connectMethod.addInterceptor(ConnectableFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR_NETTY));
            }

            return target.toBytecode();
        }
    }

    // reactor.core.publisher.MonoIgnoreThen
    public static class CoreSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (parameterTypes != null || parameterTypes.length > 0) {
                    constructorMethod.addInterceptor(CoreSubscriberConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class FunctionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod applyMethod = target.getDeclaredMethod("apply", "java.lang.Object");
            if (applyMethod != null) {
                applyMethod.addInterceptor(FuncationApplyInterceptor.class);
            }

            return target.toBytecode();
        }
    }
}