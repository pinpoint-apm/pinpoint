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

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.util.DelegateEnumeration;

import java.util.*;

public class DelegateEnumerationTest {

    @Te    t
	public void testNormal() throws Except       on {
		Hashtable<String, String> hashTable = new Hashtable<String,       String>();
		hashTabl       .put("a", "aa");
		ha       hTable.put("b", "bb");       		hashTable.put("c", "cc");

		List<String> valueList = new Arra       List<String>(hashTable.values());

		Enumeration<St       ing> enumeration = hashTable.elements();
		DelegateEnumeration<String> delegateEnumeration =       new DelegateEnumeration<String>(enumeration);

		Asse       t.assertTrue(delegateEnumeration.hasMoreElements());
		Assert.assert       rue(valueList.remove(delegateEnumeration.nextElement(       ));

		Assert.assertTrue(delegateEnumeration.hasMoreElements());
		A       sert.assertTrue(valueList.remove(delegateEnumeration.       extElement()));

		Assert.assertTrue(delegateEnumeration.hasMoreElem       nts());
		Assert.assertTrue(valueLis       .remove(delegateEnumeration.nextElement()));

		Assert       assertTrue(valueList.isEmpty());

		Assert.assertFalse(delegateEnumeration.ha    M    reE    ements());
		assertNextElements_Expecte       _ExceptionEmulation(enumeration, delegateEnumeration);
	}


	@Test       	public void testSkip       ) throws Exception {
       	Hashtable<String, Str       ng> hashTable = new Hashtable<String, String>();
		hashTable.put       "a", "aa");
		hashTable.put("b", "bb");
		hashTable       put("c", "cc");

		List<String> valueList = new ArrayList<String>(hashTable.values());

		Enumeration<String> enumeration = hashTable          ele          ents();
		DelegateEnumerati             n<String> del                ga                                              eEnumeration = new DelegateEnumeration<String>(enu       eration, new DelegateEnumeration.Filter<String>() {
			@Override
			       ublic boolean filter(String s) {
				if ("bb".equals(       )) {
					return true;
				}
				return false;
			}
		});

		Assert.       ssertTrue(delegateEnumeration.hasMoreE       ements());
		Assert.assertTrue(valueList.remove(delega       eEnumeration.nextElement()));

		Assert.assertTrue(delegateEnumeration.hasMor       Elements());
		Assert.assertTrue(value       ist.remove(delegateEnumeration.nextEleme        ())    ;

		Assert.assertEquals(valueList.size(), 1);

		Assert.a       sertFalse(delegateEnumeration.hasMoreElements());
		assertNextEleme       ts_Expected_ExceptionEmulation(enumeration, delegat       Enumeration);
		Assert.assertEquals(valueList.size(), 1);

		Assert.assertEquals(valueList.g       t(0), "bb");
	}

	@Test
	public void testExceptionTest       Exception() throws Exception {
		Hashtable<String, Str       ng> hashTable = new Hashtable<String, String>();

		Enu       eration<String> enumeration = hashTable.elements();
		DelegateEnumeration<Str       ng> delegateEnumeration = new DelegateEnumeration<Strin       >(enumeration);

		Assert.assertFalse(delegateEnumeration.hasMoreElements());       		Assert.assertFalse(delegateEnumeration.hasMoreElements());
		Assert.assertF       lse(delegateEnumeration.hasMoreElements());

		assertN        tEl    ments_Expected_ExceptionEmulation(enumeration, delegateEnume       ation);
		Assert.assertFalse(delegateEnumeratio       .hasMoreElements());

		assertNextElements_Expecte       _ExceptionEmulation(enumeration, delegateEnumeration);
		assertNextEl       ments_Expected_ExceptionEmulation(enumeration, delegateEnumeration);
		Assert.assertFalse(de       egateEnumeration.hasMoreElements());
	}

	@Test
	public void testExceptionTest_Excepti       n2() throws Exception {

		Enumeration enumeration = mock(Enumeration.class);

		when(e       umeration.hasMoreElements()).thenReturn(false);
		when(enumeration.nextElemen       ()).thenThrow(new NoSuchElementException());

		DelegateEnumeration<String> delegateEnu       eration = new DelegateEnumeration<String>(enumeration);

		Assert.assertEqual       (enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());
		Ass       rt.assertEquals(enumeration.hasMoreElements(), delegateEnumeration.hasMoreElements());        		a    sertNextElements_Expected_ExceptionEmulation(enumerat       on, delegateEnumeration);
		Assert.assertEquals       enumeration.hasMoreElements(), delegateEnumeration       hasMoreElements());

		assertNextElements_Expec       ed_ExceptionEmulation(enumeration, delegateEnumeration);
		assertNextElements_Expected_Excep       ionEmulation(enumeration, delegateEnumeration);
		Asse       t.assertEquals(enumeration.hasMoreElements(), delegateEn       meration.hasMoreElements());
	}

	@Test
	public void te       tExceptionTest_Null() throws Exception {
		Enumeration        numeration = mock(Enumeration.class);

		when(enumerati        .ha    MoreElements()).thenReturn(false);
		when(enumeration.       extElement()).thenReturn(null);


		DelegateEnumeration<S          ring> delegateEnumerati          n =          new DelegateEnumeration<Stri             g>(en                             eration);

		Assert.ass             rtFa                se(d                legat                                              Enumeration.hasMoreElements());
		Assert.assertFalse(delegateEnumeration.hasMoreElements       ));


		Assert.assertSame(delegateEnumeration.nextEle       ent(), null);
		Assert.assertSame(delegateEnumeration.       extElement(), null);
		Assert.assertFalse(delegateEnumerat       on.hasMoreElements());

	}

	@Test
	public void testExc       ptionTest_Null2() throws Exception {
		Enumeration<Stri       g> enumeration = new Enumeration<String>() {
			private       boolean first = true;
			@Override
			public boolean ha    Mo    eElements() {
				return first;
			}

			@Override
			public String nextElement() {
				if (first) {
					first = false;
					return "exis       ";
				}
				return null;
			}
		};


		       elegateEnumeration<String> delegat       Enumeration = new DelegateEnumeration<String>(enume       ation);

		Assert.assertTrue(deleg       teEnumeration.hasMoreElements());
		Assert.assertTrue(dele       ateEnumeration.hasMoreElements());

		Assert.assertSame(delega       eEnumeration.nextElement(), "exist");
		Assert.assertFalse    del    gateEnumeration.hasMoreElements());

		Assert.assert       a          e(delegateEnumera       ion.nextElement(),           ull             ;
		Assert.assertSame(delegateEn       meration    nextElement(), null);
		Assert.assertFalse(delegateEnumeration.hasMoreElements());

	}



	private void assertNextElements_Expected_ExceptionEmulation(Enumeration<String> elements, DelegateEnumeration<String> delegateEnumeration) {
		Exception original = getException(elements);
		Assert.assertNotSame(original, null);

		Exception delegate = getException(delegateEnumeration);
		Assert.assertNotSame(delegate, null);

		Assert.assertEquals(original.getClass(), delegate.getClass());
		Assert.assertEquals(original.getMessage(), delegate.getMessage());
		Assert.assertEquals(original.getCause(), delegate.getCause());
	}




	private Exception getException(Enumeration elements) {
		try {
			elements.nextElement();
		} catch (Exception e) {
			return e;
		}
		Assert.fail("NoSuchElementException");
		return null;
	}


}