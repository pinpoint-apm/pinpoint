package com.nhn.pinpoint.testweb.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.repository.MemberDao;

@RunWith(MockitoJUnitRunner.class)
public class MemberServiceImplTest {

	MemberServiceImpl memberService;
	@Mock
	MemberDao dao;

	@Before
	public void setUp() {
		memberService = new MemberServiceImpl();
        memberService.setDao(dao);
	}

	@Test
	public void mockup() {
		assertThat(memberService.getDao(), is(notNullValue()));
	}

	/**
	 * http://mockito.googlecode.com/svn/tags/latest/javadoc/org/mockito/Mockito
	 * .html
	 */
	@Test
	public void add() {
		Member member = new Member();
		member.setId(1);
		member.setName("keesun");
		memberService.add(member);
		verify(dao).add(member);
	}

}
