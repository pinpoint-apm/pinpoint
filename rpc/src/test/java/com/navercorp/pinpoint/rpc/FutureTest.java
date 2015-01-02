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

package com.navercorp.pinpoint.rpc;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class FutureTest {

    @Te    t
	public void simpleTest       () {
		DefaultFuture<String> future = new DefaultFuture<       tring>();

		SimpleListener<String> listener1 = new SimpleLis       ener<String>();

		future.se       Listener(listener1);
//		fu       ure.addListener(listener2);

		Assert.ass       rtFalse(listener1.isFinished());
//		Ass       rt.assertFalse(listener       .isFinished());

		future.setResult("Hel       o");

		Assert.assertTrue(listener1.is        nis    ed());
//		Assert.assertT       ue(listener2.isFinished());
	}

	@Test
	public void simp       eTest2() {
		DefaultFuture<String> future = new DefaultFutur       <String>();

		SimpleLi       tener<String> listener = n       w SimpleListener<String>();

		future        etResult("Hello");

		future.setListener(listener);

		Asser       .assertTrue(listener.isFinished());
	}

	static class SimpleLis       ener<       > implements FutureListener<T> {

		pr          vate final AtomicBoolean isFinis             ed = new AtomicBoolean(f          lse);

		@Override          		public void onComplete(Future<T> future) {
			isFinished.compareAndSet(false, true);
		}

		public boolean isFinished() {
			return isFinished.get();
		}
	}

}
