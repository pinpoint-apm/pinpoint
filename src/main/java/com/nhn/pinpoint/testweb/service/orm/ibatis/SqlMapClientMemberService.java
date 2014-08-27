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
@Service("sqlMapClientMemberService")
@Transactional("mysqlTransactionManager")
public class SqlMapClientMemberService implements MemberService {

	@Autowired
	@Qualifier("sqlMapClientMemberDao")
	private MemberDao sqlMapClientMemberDao;

	@Override
	public void add(Member member) {
		this.sqlMapClientMemberDao.add(member);
	}

	@Override
	public void addStatement(Member member) {
		this.sqlMapClientMemberDao.addStatement(member);
	}

	@Override
	public void update(Member member) {
		this.sqlMapClientMemberDao.update(member);
	}

	@Override
	public Member get(int id) {
		return this.sqlMapClientMemberDao.get(id);
	}

	@Override
	public List<Member> list() {
		return this.sqlMapClientMemberDao.list();
	}

	@Override
	public void delete(int id) {
		this.sqlMapClientMemberDao.delete(id);
	}

}
