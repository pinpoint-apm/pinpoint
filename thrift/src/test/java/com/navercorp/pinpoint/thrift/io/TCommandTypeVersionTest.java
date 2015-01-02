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

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.thrift.io.TCommandType;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin
 */
public class TCommandTypeVersionTest {

    @Te    t
	public void versionTest       () {
		TCommandTypeVersion version = TCommandTypeVersion.V_1_0_             _SNAPSHOT;
		
		List<TCommandType> supportTypeList = version.ge       SupportCommandList();

		Assert.assertEqual       (2, supportTypeList.size());
		Assert.assertTrue(supportTypeList.c       ntains(TCommandType.THREAD_DUMP));
		Assert.assertTrue(suppor        ype    ist.contains(TCommandType.       ESULT));
	}

	@Test
	public void versionTest2() {
		TC             mmandTypeVersion version = TCommandTypeVersion.UNKNOWN;
		
		Li       t<TCommandType> supportTypeList = version.g          tS    pportCommandList();

		Ass       rt.assertEquals(0, supportTypeList.size());
	}
	
	@Test
	public vo       d versionTest3() {
		TCommandTypeVersion version = TCo             mandTypeVersion.getVersion("1.0.0");
		Assert.assertEquals(TCommandTypeVersion.UNKNOWN, v       rsion);
		
		version = TCommandTypeVersion.getVersion(TCommandT          peVersion.V_1_0_2_SNAPSHOT.getVersionName());
		Assert.assertEquals(TCommandTypeVersion.V_1_0_2_SNAPSHOT, version);
	}
	
	
}
