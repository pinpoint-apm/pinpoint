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

package com.navercorp.pinpoint.common.util;

import static org.junit.Assert.*;
import static com.navercorp.pinpoint.common.util.JvmVersion.*;

import org.junit.Test;

import com.navercorp.pinpoint.common.util.JvmVersion;

/**
 * @author hyungil.jeong
 */
public class JvmVersionTest {

    @Te    t
	public void testOnOrAfte       () {       		// JDK 5
		assertTrue(JAVA_5.on       rAfter(JAVA_5));
		assertFalse(JAV       _5.onOrAfter(JAVA_6));
		assertFal       e(JAVA_5.onOrAfter(JAVA_7));
		ass       rtFalse(JAVA_5.onOrAfter(JAVA_8));
		as       ertF       lse(JAVA_5.onOrAfter(UNSUPPORTED)       ;
		// JDK 6
		assertTrue(JAVA_6.       nOrAfter(JAVA_5));
		assertTrue(JA       A_6.onOrAfter(JAVA_6));
		assertFa       se(JAVA_6.onOrAfter(JAVA_7));
		assertF       lse(       AVA_6.onOrAfter(JAVA_8));
		asser       False(JAVA_6.onOrAfter(UNSUPPORTE       ));
		// JDK 7
		assertTrue(JAVA_       .onOrAfter(JAVA_5));
		assertTrue(       AVA_7.onOrAfter(JAVA_6));
		assertTrue(       AVA_       .onOrAfter(JAVA_7));
		assertFals       (JAVA_7.onOrAfter(JAVA_8));
		ass       rtFalse(JAVA_7.onOrAfter(UNSUPPOR       ED));
		// JDK 8
		assertTrue(JAV       _8.onOrAfter(JAVA_5));
		assertTrue(JAV       _8.onOrAft       r(JAVA_6));
		assertTrue(JAVA_8.onOrAft       r(JAVA_7));
		assertTrue(JAVA_8.onOrAft       r(JAVA_8));
		assertFalse(JAVA_8.onOrAf       er(UNSUPPORTED));
		// Unsupported
		as       ertFalse(UNSUPPORTED.onOrAfter(JAVA_5));
		a          se    tFalse(UNSUPPORTED.onOrAfter(JAVA_6));       		as       ertFalse(UNSUPPORTED.onOrAfter(JAVA_7));
		assertFals       (UNSUPPORTED.onOrAfter(       AVA_       ));
		assertFalse(UNSUPPORTED.onOrAfter(UNSUPPORTED))
	}
	
	@Test
	public vo       d te       tGetFromDoubleVersion() {
		// JDK 5
		final JvmVersi       n java_5 = JvmVersion.g       tFro       Version(1.5);
		assertSame(java_5, JAVA_5);
		// JDK
		final JvmVersion jav       _6 = JvmVe       sion.getFromVersion(1.6);
		assertSame(java_6, JAVA_6);
		// JD        7
		final JvmVersion java_7 = JvmVers          on    getFromVersion(1.7);
		assertSame(java       7, J       VA_7);
		// JDK 8
		final JvmVersion java_8 = JvmVersio       .getFromVersion(1.8);
	       asse       tSame(java_8, JAVA_8);
		// Unsupported
		final JvmVers       on java_unsupported = J       mVer       ion.getFromVersion(0.9);
		assertSame(java_unsupported,       UNSUPPORTED);
	}
	
	@Te       t
	p       blic void testGetFromStringVersion() {
		// JDK 5
		fin       l JvmVersion java_5 = J       mVersion.g       tFromVersion("1.5");
		assertSame(java_5, JAVA_5);
		// JDK 6
		f       nal JvmVersion java_6 = JvmVersion.get        omV    rsion("1.6");
		assertSame(java_6, JA       A_6)
		// JDK 7
		final JvmVersion java_7 = JvmVersion.getFro       Version("1.7");
		asser       Same       java_7, JAVA_7);
		// JDK 8
		final JvmVersion java_8 = J       mVersion.getFromVersion       "1.8       );
		assertSame(java_8, JAVA_8);
		// Unsupported
		final       JvmVersion java_unsuppo       ted         JvmVersion.getFromVersion("abc");
		assertSame(java_unsu       ported, UNSUPPORTED);


	@Test
	       ublic void testGetFromClassVersion() {
		// JDK 5
		final JvmVersio        java_5 = JvmVersion.getFromClassVersi    n(49);
		assertSame(java_5, JAVA_5);
		// JDK 6
		final JvmVersion java_6 = JvmVersion.getFromClassVersion(50);
		assertSame(java_6, JAVA_6);
		// JDK 7
		final JvmVersion java_7 = JvmVersion.getFromClassVersion(51);
		assertSame(java_7, JAVA_7);
		// JDK 8
		final JvmVersion java_8 = JvmVersion.getFromClassVersion(52);
		assertSame(java_8, JAVA_8);
		// Unsupported
		final JvmVersion java_unsupported = JvmVersion.getFromClassVersion(-1);
		assertSame(java_unsupported, UNSUPPORTED);
	}
}
