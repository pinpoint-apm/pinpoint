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

package com.navercorp.pinpoint.thrift.io;

import junit.framework.Assert;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Test;

import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.navercorp.pinpoint.thrift.io.Header;
import com.navercorp.pinpoint.thrift.io.TCommandRegistry;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin
 */
public class TCommandRegistryTest {

    @Te    t
	public void registryTest       () {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion       UNKNOWN);

		Assert.assertFalse(registry.isSupport(TCommandType.RE       ULT.getType()));
		Assert.assertFalse(registry.isSupport(TCommandType.TH       EAD_DUMP.getType()));

		Assert.assertFalse(regist       y.isSupport(TResult.class));
		Assert.assertFalse(registry.is        pport(TCommandThreadDump.class))
	}

	@Test(expected = TException.class)
	pub       ic void registryTest2() throws TException {
		TCommandRegistry registry = n       w TCommandRegistry(TCommandTypeVe        ion.UNKNOWN);

		registry.header    ookup(new TResult());
	}

	@Test(expected = T       xception.class)
	public void registryTest3() throws TException {
		TCommand       egistry registry = new TCommandRegistry(TCommand        peV    rsion.UNKNOWN);

		registry       tBaseLookup(TCommandType.RESULT.getType());
	}

	@Test
	public void registryTest4()
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeV       rsion.V_1_0_2_SNAPSHOT);

		Assert.assertTrue(registry.isSupport(TComma       dType.RESULT.getType()));
		Assert.assertTrue(reg       stry.isSupport(TCommandType.THREAD_DUMP.getType()));

		Asse          t.    ssertTrue(registry.isSupport(TResult.class));       		Assert.assertTrue(registry.isSupport(TCommandThreadDump.class));
	}
	
	@Test
	publ       c void registryTest5() throws TException {
		TCom       andRegistry registry = ne           T    ommandRegistry(TCommandTypeVersion.V_1_0_2_SN       PSHOT);

		Header header = registry.headerLookup(new TResult());
		Assert.assertNotN       ll(header);
	}
	
	@Test
	public void registryTest6() throws TE       ception {
		TCommandRegistry registry = new TComm             ndRegistry(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

		TBase       tBase = registry.tBaseLookup(TCommandType.RESULT.getType());    		Assert.assertEquals(tBase.getClass(), TResult.class);
		
		tBase = registry.tBaseLookup(TCommandType.THREAD_DUMP.getType());
		Assert.assertEquals(tBase.getClass(), TCommandThreadDump.class);
	}

}
