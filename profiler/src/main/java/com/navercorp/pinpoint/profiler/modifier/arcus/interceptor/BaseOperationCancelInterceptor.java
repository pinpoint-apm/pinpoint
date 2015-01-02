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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.MetaObject;
import com.navercorp.pinpoint.bootstrap.util.TimeObject;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncTrace;

import net.spy.memcached.protocol.BaseOperationImpl;

/**
 * @author emeroad
 */
@Deprecated
public class BaseOperationCancelInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled()

	private MetaObject getAsyncTrace = new MetaObject("__getAsyncTrac    ");

	@    verride
	public void before(Object target, Objec       [] args) {          		if (isDebug) {
			logger.before             nterceptor(target, args);
		}

		AsyncTrace asyncTrace = (Asy       cTrace) getAsyncTrace          invoke(target);
		if (asyncTrace          =              null) {
			logger.debug("asyncTrace not found ");
			r          turn;
		}

		if (asyncTra          e             getState() != DefaultAsyncTrace.STATE_INIT) {
			// Oper       tion already completed.
			retu          n;
		}

		BaseOperationImpl baseOperation = (BaseOperationIm          l) target;
		if (!base          peration.isCancelled()) {
			TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
			timeObject.markCancelTime();
		}
	}

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
