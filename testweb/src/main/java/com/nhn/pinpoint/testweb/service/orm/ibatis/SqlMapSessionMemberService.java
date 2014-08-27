package com.nhn.pinpoint.testweb.service.orm.ibatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.repository.MemberDao;
import com.nhn.pinpoint.testweb.service.MemberService;

/**
 * @author Hyun Jeong
 */
@Service("sqlMapSessionMemberService")
@Transactional("mysqlTransactionManager")
public class SqlMapSessionMemberService implements MemberService {

	@Autowired
	@Qualifier("sqlMapSessionMemberDao")
	private MemberDao sqlMapSessionMemberDao;
	
	@Override
	public void add(Member member) {
		this.sqlMapSessionMemberDao.add(member);
	}

	@Override
	public void addStatement(Member member) {
		this.sqlMapSessionMemberDao.addStatement(member);
	}

	@Override
	public void update(Member member) {
		this.sqlMapSessionMemberDao.update(member);
	}

	@Override
	public Member get(int id) {
		return this.sqlMapSessionMemberDao.get(id);
	}

	@Override
	public List<Member> list() {
		return this.sqlMapSessionMemberDao.list();
	}

	@Override
	public void delete(int id) {
		this.sqlMapSessionMemberDao.delete(id);
	}

}
