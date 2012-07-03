package com.nhn.hippo.testweb.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.nhn.hippo.testweb.DBUnitSupport;
import com.nhn.hippo.testweb.domain.Member;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/servlet-context.xml")
@Transactional
public class MemberDaoIbatisTest extends DBUnitSupport {

	@Autowired
	MemberDaoIbatis memberDao;

	@Test
	public void di() {
		assertThat(memberDao, is(notNullValue()));
	}

	@Test
	public void crud() {
		Member member = new Member();
		member.setId(1);
		member.setName("whiteship");
		member.setJoined(new Date());
		memberDao.add(member);
		assertThat(memberDao.list().size(), is(1));

		member.setName("ê¸°ì?");
		memberDao.update(member);
		assertThat(memberDao.get(1).getName(), is("ê¸°ì?"));

		memberDao.delete(1);
		assertThat(memberDao.list().size(), is(0));
	}

}
