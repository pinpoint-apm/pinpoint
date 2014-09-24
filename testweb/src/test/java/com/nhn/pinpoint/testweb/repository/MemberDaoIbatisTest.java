package com.nhn.pinpoint.testweb.repository;

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

import com.nhn.pinpoint.testweb.DBUnitSupport;
import com.nhn.pinpoint.testweb.domain.Member;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/applicationContext-testweb.xml", "/servlet-context.xml" })
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
		member.setName("netspider");
		member.setJoined(new Date());
		memberDao.add(member);
		assertThat(memberDao.list().size(), is(1));

		member.setName("chisu");
		memberDao.update(member);
		assertThat(memberDao.get(1).getName(), is("chisu"));

		memberDao.delete(1);
		assertThat(memberDao.list().size(), is(0));
	}

}
