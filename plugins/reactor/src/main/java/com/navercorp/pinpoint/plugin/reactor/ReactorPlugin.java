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
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeOrReturnInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
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
        addFluxOperatorTransform("reactor.core.publisher.FluxPublishOn");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.FluxPublishOn$PublishOnConditionalSubscriber");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.FluxPublishOn$PublishOnSubscriber");
        // subscribeOn
        addFluxTransform("reactor.core.publisher.FluxSubscribeOnValue");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.FluxSubscribeOnValue$ScheduledEmpty");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.FluxSubscribeOnValue$ScheduledScalar");
        addFluxTransform("reactor.core.publisher.FluxSubscribeOnCallable");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.FluxSubscribeOnCallable$CallableSubscribeOnSubscription");
        addFluxOperatorTransform("reactor.core.publisher.FluxSubscribeOn");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.FluxSubscribeOn$SubscribeOnSubscriber");

        // Flux
        addConnectableFluxTransform("reactor.core.publisher.ConnectableFluxHide");
        addConnectableFluxTransform("reactor.core.publisher.ConnectableFluxOnAssembly");
        addConnectableFluxTransform("reactor.core.publisher.ConnectableLift");
        addConnectableFluxTransform("reactor.core.publisher.ConnectableLiftFuseable");
        addFluxTransform("reactor.core.publisher.FluxArray");
        addConnectableFluxTransform("reactor.core.publisher.FluxAutoConnect");
        addConnectableFluxTransform("reactor.core.publisher.FluxAutoConnectFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxBuffer");
        addFluxOperatorTransform("reactor.core.publisher.FluxBufferBoundary");
        addFluxOperatorTransform("reactor.core.publisher.FluxBufferPredicate");
        addFluxOperatorTransform("reactor.core.publisher.FluxBufferTimeout");
        addFluxOperatorTransform("reactor.core.publisher.FluxBufferWhen");
        addFluxTransform("reactor.core.publisher.FluxCallable");
        addFluxOperatorTransform("reactor.core.publisher.FluxCallableOnAssembly");
        addFluxOperatorTransform("reactor.core.publisher.FluxCancelOn");
        addFluxTransform("reactor.core.publisher.FluxCombineLatest");
        addFluxTransform("reactor.core.publisher.FluxConcatArray");
        addFluxTransform("reactor.core.publisher.FluxConcatIterable");
        addFluxOperatorTransform("reactor.core.publisher.FluxConcatMap");
        addFluxOperatorTransform("reactor.core.publisher.FluxConcatMapNoPrefetch");
        addFluxOperatorTransform("reactor.core.publisher.FluxContextWrite");
        addFluxTransform("reactor.core.publisher.FluxCreate");
        addFluxOperatorTransform("reactor.core.publisher.FluxDefaultIfEmpty");
        addFluxTransform("reactor.core.publisher.FluxDefer");
        addFluxTransform("reactor.core.publisher.FluxDeferContextual");
        addFluxOperatorTransform("reactor.core.publisher.FluxDelaySequence");
        addFluxDelaySubscriptionTransform("reactor.core.publisher.FluxDelaySubscription");
        addFluxOperatorTransform("reactor.core.publisher.FluxDematerialize");
        addFluxOperatorTransform("reactor.core.publisher.FluxDetach");
        addFluxOperatorTransform("reactor.core.publisher.FluxDistinct");
        addFluxOperatorTransform("reactor.core.publisher.FluxDistinctFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxDistinctUntilChanged");
        addFluxOperatorTransform("reactor.core.publisher.FluxDoFinally");
        addFluxOperatorTransform("reactor.core.publisher.FluxDoFinallyFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxDoFirst");
        addFluxOperatorTransform("reactor.core.publisher.FluxDoFirstFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxDoOnEach");
        addFluxOperatorTransform("reactor.core.publisher.FluxDoOnEachFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxElapsed");
        addFluxOperatorTransform("reactor.core.publisher.FluxEmpty");
        addFluxTransform("reactor.core.publisher.FluxError");
        addFluxTransform("reactor.core.publisher.FluxErrorOnRequest");
        addFluxTransform("reactor.core.publisher.FluxErrorSupplied");
        addFluxOperatorTransform("reactor.core.publisher.FluxExpand");
        addFluxOperatorTransform("reactor.core.publisher.FluxFilter");
        addFluxOperatorTransform("reactor.core.publisher.FluxFilterFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxFilterWhen");
        addFluxTransform("reactor.core.publisher.FluxFirstWithSignal");
        addFluxTransform("reactor.core.publisher.FluxFirstWithValue");
        addFluxOperatorTransform("reactor.core.publisher.FluxFlatMap");
        addFluxOperatorTransform("reactor.core.publisher.FluxFlattenIterable");
        addFluxOperatorTransform("reactor.core.publisher.FluxFromMonoOperator");
        addFluxTransform("reactor.core.publisher.FluxGenerate");
        addFluxOperatorTransform("reactor.core.publisher.FluxGroupBy");
        addFluxOperatorTransform("reactor.core.publisher.FluxGroupJoin");
        addFluxOperatorTransform("reactor.core.publisher.FluxHandle");
        addFluxOperatorTransform("reactor.core.publisher.FluxHandleFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxHide");
        addFluxOperatorTransform("reactor.core.publisher.FluxIndex");
        addFluxOperatorTransform("reactor.core.publisher.FluxIndexFuseable");
        addFluxTransform("reactor.core.publisher.FluxInterval");
        addFluxTransform("reactor.core.publisher.FluxIterable");
        addFluxOperatorTransform("reactor.core.publisher.FluxJoin");
        addFluxTransform("reactor.core.publisher.FluxJust");
        addFluxOperatorTransform("reactor.core.publisher.FluxLift");
        addFluxOperatorTransform("reactor.core.publisher.FluxLiftFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxLimitRequest");
        addFluxOperatorTransform("reactor.core.publisher.FluxLog");
        addFluxOperatorTransform("reactor.core.publisher.FluxLogFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxMap");
        addFluxOperatorTransform("reactor.core.publisher.FluxMapFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxMapSignal");
        addFluxOperatorTransform("reactor.core.publisher.FluxMaterialize");
        addFluxTransform("reactor.core.publisher.FluxMerge");
        addFluxTransform("reactor.core.publisher.FluxMergeComparing");
        addFluxOperatorTransform("reactor.core.publisher.FluxMergeSequential");
        addFluxOperatorTransform("reactor.core.publisher.FluxMetrics");
        addFluxOperatorTransform("reactor.core.publisher.FluxMetricsFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxName");
        addFluxOperatorTransform("reactor.core.publisher.FluxNameFuseable");
        addFluxTransform("reactor.core.publisher.FluxNever");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnAssembly");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnBackpressureBuffer");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnBackpressureBufferStrategy");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnBackpressureBufferTimeout");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnBackpressureDrop");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnBackpressureLatest");
        addFluxOperatorTransform("reactor.core.publisher.FluxOnErrorResume");
        addFluxOperatorTransform("reactor.core.publisher.FluxPeek");
        addFluxOperatorTransform("reactor.core.publisher.FluxPeekFuseable");
        addConnectableFluxTransform("reactor.core.publisher.FluxPublish");
        addFluxOperatorTransform("reactor.core.publisher.FluxPublishMulticast");
        addFluxTransform("reactor.core.publisher.FluxRange");
        addFluxTransform("reactor.core.publisher.FluxRefCount");
        addFluxTransform("reactor.core.publisher.FluxRefCountGrace");
        addFluxOperatorTransform("reactor.core.publisher.FluxRepeat");
        addFluxOperatorTransform("reactor.core.publisher.FluxRepeatPredicate");
        addFluxOperatorTransform("reactor.core.publisher.FluxRepeatWhen");
        addConnectableFluxTransform("reactor.core.publisher.FluxReplay");
        addFluxOperatorTransform("reactor.core.publisher.FluxRetry");
        addFluxOperatorTransform("reactor.core.publisher.FluxRetryPredicate");
        addFluxOperatorTransform("reactor.core.publisher.FluxRetryWhen");
        addFluxOperatorTransform("reactor.core.publisher.FluxSample");
        addFluxOperatorTransform("reactor.core.publisher.FluxSampleFirst");
        addFluxOperatorTransform("reactor.core.publisher.FluxSampleTimeout");
        addFluxOperatorTransform("reactor.core.publisher.FluxScan");
        addFluxOperatorTransform("reactor.core.publisher.FluxScanSeed");
        addFluxOperatorTransform("reactor.core.publisher.FluxSkip");
        addFluxOperatorTransform("reactor.core.publisher.FluxSkipLast");
        addFluxOperatorTransform("reactor.core.publisher.FluxSkipUntil");
        addFluxOperatorTransform("reactor.core.publisher.FluxSkipWhile");
        addFluxTransform("reactor.core.publisher.FluxSource");
        addFluxOperatorTransform("reactor.core.publisher.FluxSourceFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxSourceMono");
        addFluxOperatorTransform("reactor.core.publisher.FluxSourceMonoFuseable");
        addFluxTransform("reactor.core.publisher.FluxStream");
        addFluxOperatorTransform("reactor.core.publisher.FluxSwitchIfEmpty");
        addFluxOperatorTransform("reactor.core.publisher.FluxSwitchMap");
        addFluxOperatorTransform("reactor.core.publisher.FluxSwitchMapNoPrefetch");
        addFluxOperatorTransform("reactor.core.publisher.FluxSwitchOnFirst");
        addFluxOperatorTransform("reactor.core.publisher.FluxTake");
        addFluxOperatorTransform("reactor.core.publisher.FluxTakeFuseable");
        addFluxOperatorTransform("reactor.core.publisher.FluxTakeLast");
        addFluxOperatorTransform("reactor.core.publisher.FluxTakeLastOne");
        addFluxOperatorTransform("reactor.core.publisher.FluxTakeUntil");
        addFluxOperatorTransform("reactor.core.publisher.FluxTakeUntilOther");
        addFluxOperatorTransform("reactor.core.publisher.FluxTakeWhile");
        addFluxOperatorTransform("reactor.core.publisher.FluxTimed");
        addFluxOperatorTransform("reactor.core.publisher.FluxTimeout");
        addFluxTransform("reactor.core.publisher.FluxUsing");
        addFluxTransform("reactor.core.publisher.FluxUsingWhen");
        addFluxOperatorTransform("reactor.core.publisher.FluxWindow");
        addFluxOperatorTransform("reactor.core.publisher.FluxWindowBoundary");
        addFluxOperatorTransform("reactor.core.publisher.FluxWindowPredicate");
        addFluxOperatorTransform("reactor.core.publisher.FluxWindowTimeout");
        addFluxOperatorTransform("reactor.core.publisher.FluxWindowWhen");
        addFluxOperatorTransform("reactor.core.publisher.FluxWithLatestFrom");
        addFluxTransform("reactor.core.publisher.FluxZip");
        addFluxOperatorTransform("reactor.core.publisher.FluxZipIterable");
        addFluxTransform("reactor.core.publisher.GroupedLift");
        addFluxOperatorTransform("reactor.core.publisher.GroupedLiftFuseable");

        addFluxTransform("reactor.core.publisher.FluxFirstEmitting");
        addFluxTransform("reactor.core.publisher.FluxMergeOrdered");
        addFluxOperatorTransform("reactor.core.publisher.FluxSkipUntilOther");
        addFluxOperatorTransform("reactor.core.publisher.FluxContextStart");
        addFluxOperatorTransform("reactor.core.publisher.FluxPublishOnCaptured");
        addFluxOperatorTransform("reactor.core.publisher.FluxSchedulerCapture");

        addFluxOperatorTransform("reactor.core.publisher.ParallelMergeOrdered");
        addFluxOperatorTransform("reactor.core.publisher.ParallelMergeSequential");
        addFluxOperatorTransform("reactor.core.publisher.ParallelMergeSort");

        addFluxTransform("reactor.core.publisher.SinkManyBestEffort");
        addFluxTransform("reactor.core.publisher.UnicastManySinkNoBackpressure");

        addConnectableFluxTransform("reactor.core.publisher.InternalConnectableFluxOperator");
        addFluxOperatorTransform("reactor.core.publisher.InternalFluxOperator");
    }

    private void addMono() {
        transformTemplate.transform("reactor.core.publisher.Mono", MonoMethodTransform.class);
        // publishOn
        addMonoOperatorTransform("reactor.core.publisher.MonoPublishOn");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.MonoPublishOn$PublishOnSubscriber");
        // subscribeOn
        addMonoTransform("reactor.core.publisher.MonoSubscribeOnValue");
        addMonoTransform("reactor.core.publisher.MonoSubscribeOnCallable");
        addMonoOperatorTransform("reactor.core.publisher.MonoSubscribeOn");
        addRunnableCoreSubscriberTransform("reactor.core.publisher.MonoSubscribeOn$SubscribeOnSubscriber");

        // mono
        addMonoOperatorTransform("reactor.core.publisher.MonoAll");
        addMonoOperatorTransform("reactor.core.publisher.MonoAny");
        addMonoOperatorTransform("reactor.core.publisher.MonoCacheInvalidateIf");
        addMonoOperatorTransform("reactor.core.publisher.MonoCacheInvalidateWhen");
        addMonoTransform("reactor.core.publisher.MonoCallable");
        addMonoOperatorTransform("reactor.core.publisher.MonoCacheTime");
        addMonoOperatorTransform("reactor.core.publisher.MonoCancelOn");
        addMonoOperatorTransform("reactor.core.publisher.MonoCollect");
        addMonoOperatorTransform("reactor.core.publisher.MonoCollectList");
        addMonoTransform("reactor.core.publisher.MonoCompletionStage");
        addMonoTransform("reactor.core.publisher.MonoContextWrite");
        addMonoOperatorTransform("reactor.core.publisher.MonoCount");
        addMonoTransform("reactor.core.publisher.MonoCreate");
        addMonoTransform("reactor.core.publisher.MonoCurrentContext");
        addMonoOperatorTransform("reactor.core.publisher.MonoDefaultIfEmpty");
        addMonoTransform("reactor.core.publisher.MonoDefer");
        addMonoTransform("reactor.core.publisher.MonoDeferContextual");
        addMonoTransform("reactor.core.publisher.MonoDelay");
        addMonoOperatorTransform("reactor.core.publisher.MonoDelayElement");
        addMonoDelaySubscriptionTransform("reactor.core.publisher.MonoDelaySubscription");
        addMonoTransform("reactor.core.publisher.MonoDelayUntil");
        addMonoOperatorTransform("reactor.core.publisher.MonoDematerialize");
        addMonoOperatorTransform("reactor.core.publisher.MonoDetach");
        addMonoOperatorTransform("reactor.core.publisher.MonoDoFinally");
        addMonoOperatorTransform("reactor.core.publisher.MonoDoFinallyFuseable");
        addMonoTransform("reactor.core.publisher.MonoFirst");
        addMonoOperatorTransform("reactor.core.publisher.MonoDoFirstFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoDoOnEach");
        addMonoOperatorTransform("reactor.core.publisher.MonoDoOnEachFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoElapsed");
        addMonoOperatorTransform("reactor.core.publisher.MonoElementAt");
        addMonoTransform("reactor.core.publisher.MonoEmpty");
        addMonoTransform("reactor.core.publisher.MonoError");
        addMonoTransform("reactor.core.publisher.MonoErrorSupplied");
        addMonoOperatorTransform("reactor.core.publisher.MonoExpand");
        addMonoOperatorTransform("reactor.core.publisher.MonoFilter");
        addMonoOperatorTransform("reactor.core.publisher.MonoFilterFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoFilterWhen");
        addMonoTransform("reactor.core.publisher.MonoFirstWithSignal");
        addMonoTransform("reactor.core.publisher.MonoFirstWithValue");
        addMonoOperatorTransform("reactor.core.publisher.MonoFlatMap");
        addMonoOperatorTransform("reactor.core.publisher.MonoFlatMapMany");
        addMonoOperatorTransform("reactor.core.publisher.MonoFlattenIterable");
        addMonoTransform("reactor.core.publisher.MonoFromFluxOperator");
        addMonoOperatorTransform("reactor.core.publisher.MonoFromPublisher");
        addMonoOperatorTransform("reactor.core.publisher.MonoHandle");
        addMonoOperatorTransform("reactor.core.publisher.MonoHandleFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoHasElement");
        addMonoOperatorTransform("reactor.core.publisher.MonoHasElements");
        addMonoOperatorTransform("reactor.core.publisher.MonoHide");
        addMonoOperatorTransform("reactor.core.publisher.MonoIgnoreElement");
        addMonoOperatorTransform("reactor.core.publisher.MonoIgnoreElements");
        addMonoOperatorTransform("reactor.core.publisher.MonoIgnorePublisher");
        addMonoTransform("reactor.core.publisher.MonoIgnoreThen");
        addMonoTransform("reactor.core.publisher.MonoJust");
        addMonoOperatorTransform("reactor.core.publisher.MonoLift");
        addMonoOperatorTransform("reactor.core.publisher.MonoLiftFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoLog");
        addMonoOperatorTransform("reactor.core.publisher.MonoLogFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoMap");
        addMonoOperatorTransform("reactor.core.publisher.MonoMapFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoMaterialize");
        addMonoOperatorTransform("reactor.core.publisher.MonoMetrics");
        addMonoOperatorTransform("reactor.core.publisher.MonoMetricsFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoName");
        addMonoOperatorTransform("reactor.core.publisher.MonoNameFuseable");
        addMonoTransform("reactor.core.publisher.MonoNever");
        addMonoOperatorTransform("reactor.core.publisher.MonoNext");
        addMonoOperatorTransform("reactor.core.publisher.MonoOnAssembly");
        addMonoOperatorTransform("reactor.core.publisher.MonoOnErrorResume");
        addMonoOperatorTransform("reactor.core.publisher.MonoPeek");
        addMonoOperatorTransform("reactor.core.publisher.MonoPeekFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoPeekTerminal");
        addMonoOperatorTransform("reactor.core.publisher.MonoPublishMulticast");
        addMonoOperatorTransform("reactor.core.publisher.MonoReduce");
        addMonoOperatorTransform("reactor.core.publisher.MonoReduceSeed");
        addMonoOperatorTransform("reactor.core.publisher.MonoRepeat");
        addMonoOperatorTransform("reactor.core.publisher.MonoRepeatPredicate");
        addMonoOperatorTransform("reactor.core.publisher.MonoRepeatWhen");
        addMonoOperatorTransform("reactor.core.publisher.MonoRetry");
        addMonoOperatorTransform("reactor.core.publisher.MonoRetryPredicate");
        addMonoOperatorTransform("reactor.core.publisher.MonoRetryWhen");
        addMonoOperatorTransform("reactor.core.publisher.MonoRunnable");
        addMonoTransform("reactor.core.publisher.MonoSequenceEqual");
        addMonoOperatorTransform("reactor.core.publisher.MonoSingle");
        addMonoOperatorTransform("reactor.core.publisher.MonoSingleCallable");
        addMonoOperatorTransform("reactor.core.publisher.MonoSingleMono");
        addMonoTransform("reactor.core.publisher.MonoSource");
        addMonoOperatorTransform("reactor.core.publisher.MonoSourceFlux");
        addMonoOperatorTransform("reactor.core.publisher.MonoSourceFluxFuseable");
        addMonoTransform("reactor.core.publisher.MonoSourceFuseable");
        addMonoOperatorTransform("reactor.core.publisher.MonoStreamCollector");
        addMonoTransform("reactor.core.publisher.MonoSupplier");
        addMonoOperatorTransform("reactor.core.publisher.MonoSwitchIfEmpty");
        addMonoOperatorTransform("reactor.core.publisher.MonoTakeLastOne");
        addMonoOperatorTransform("reactor.core.publisher.MonoTakeUntilOther");
        addMonoOperatorTransform("reactor.core.publisher.MonoTimed");
        addMonoOperatorTransform("reactor.core.publisher.MonoTimeout");
        addMonoTransform("reactor.core.publisher.MonoUsing");
        addMonoTransform("reactor.core.publisher.MonoUsingWhen");
        addMonoTransform("reactor.core.publisher.MonoWhen");
        addMonoTransform("reactor.core.publisher.MonoZip");

        addMonoOperatorTransform("reactor.core.publisher.MonoSubscriberContext");
        addMonoOperatorTransform("reactor.core.publisher.ParallelMergeReduce");
        addMonoOperatorTransform("reactor.core.publisher.ParallelThen");

        addMonoOperatorTransform("reactor.core.publisher.SinkEmptyMulticast");
        addMonoOperatorTransform("reactor.core.publisher.InternalMonoOperator");
    }

    private void addParallelFlux() {
        transformTemplate.transform("reactor.core.publisher.ParallelFlux", ParallelFluxMethodTransform.class);
        // runOn
        addParallelFluxTransform("reactor.core.publisher.ParallelRunOn");

        addParallelFluxTransform("reactor.core.publisher.ParallelArraySource");
        addParallelFluxTransform("reactor.core.publisher.ParallelCollect");
        addParallelFluxTransform("reactor.core.publisher.ParallelConcatMap");
        addParallelFluxTransform("reactor.core.publisher.ParallelDoOnEach");
        addParallelFluxTransform("reactor.core.publisher.ParallelFilter");
        addParallelFluxTransform("reactor.core.publisher.ParallelFlatMap");
        addParallelFluxTransform("reactor.core.publisher.ParallelFluxHide");
        addParallelFluxTransform("reactor.core.publisher.ParallelFluxName");
        addParallelFluxTransform("reactor.core.publisher.ParallelFluxOnAssembly");
        addParallelFluxTransform("reactor.core.publisher.ParallelGroup");
        addParallelFluxTransform("reactor.core.publisher.ParallelLift");

        addParallelFluxTransform("reactor.core.publisher.ParallelLiftFuseable");
        addParallelFluxTransform("reactor.core.publisher.ParallelLog");
        addParallelFluxTransform("reactor.core.publisher.ParallelMap");
        addParallelFluxTransform("reactor.core.publisher.ParallelPeek");
        addParallelFluxTransform("reactor.core.publisher.ParallelReduceSeed");
        addParallelFluxTransform("reactor.core.publisher.ParallelSource");
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

    void addFluxTransform(String className) {
        transformTemplate.transform(className, FluxTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(FluxConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    void addFluxOperatorTransform(String className) {
        transformTemplate.transform(className, FluxOperatorTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(FluxOperatorConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxOperatorSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    void addFluxDelaySubscriptionTransform(String className) {
        transformTemplate.transform(className, FluxDelaySubscriptionTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(FluxDelaySubscriptionConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }
            final InstrumentMethod acceptMethod = target.getDeclaredMethod("accept", "reactor.core.publisher.FluxDelaySubscription$DelaySubscriptionOtherSubscriber");
            if (acceptMethod != null) {
                acceptMethod.addInterceptor(FluxDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }

            return target.toBytecode();
        }
    }

    void addRunnableCoreSubscriberTransform(String className) {
        transformTemplate.transform(className, RunnableCoreSubscriberTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
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

    void addMonoTransform(String className) {
        transformTemplate.transform(className, MonoTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(MonoConstructorInterceptor.class);
                }
            }
            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(MonoSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    void addMonoOperatorTransform(String className) {
        transformTemplate.transform(className, MonoOperatorTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(MonoOperatorConstructorInterceptor.class);
                }
            }
            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(MonoOperatorSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    void addMonoDelaySubscriptionTransform(String className) {
        transformTemplate.transform(className, MonoDelaySubscriptionTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(MonoDelaySubscriptionConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(MonoDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }
            final InstrumentMethod acceptMethod = target.getDeclaredMethod("accept", "reactor.core.publisher.FluxDelaySubscription$DelaySubscriptionOtherSubscriber");
            if (acceptMethod != null) {
                acceptMethod.addInterceptor(MonoDelaySubscriptionSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
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

    void addParallelFluxTransform(String className) {
        transformTemplate.transform(className, ParallelFluxTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(ParallelFluxConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber[]");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(ParallelFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
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
                subscribeMethod.addInterceptor(ProcessorSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }

            return target.toBytecode();
        }
    }

    void addConnectableFluxTransform(String className) {
        transformTemplate.transform(className, ConnectableFluxTransform.class);
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
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(ConnectableFluxConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(ConnectableFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
            }
            final InstrumentMethod connectMethod = target.getDeclaredMethod("connect", "java.util.function.Consumer");
            if (connectMethod != null) {
                connectMethod.addInterceptor(ConnectableFluxSubscribeInterceptor.class, va(ReactorConstants.REACTOR));
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
                if (ArrayUtils.hasLength(parameterTypes)) {
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