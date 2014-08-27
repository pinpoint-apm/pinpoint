package com.nhn.pinpoint.testweb.service.orm.mybatis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.repository.mybatis.MemberMapper;
import com.nhn.pinpoint.testweb.service.MemberService;

/**
 * @author Hyun Jeong
 */
@Service("myBatisMemberService")
@Transactional("mysqlTransactionManager")
public class MyBatisMemberService implements MemberService {

	@Autowired
	private MemberMapper memberMapper;
	
	@Override
	public void add(Member member) {
		this.memberMapper.insertUser(member);
	}

	@Override
	public void addStatement(Member member) {
		this.memberMapper.insertUser(member);
	}

	@Override
	public void update(Member member) {
		this.memberMapper.updateUser(member);
	}

	@Override
	public Member get(int id) {
		return this.memberMapper.selectUser(id);
	}

	@Override
	public List<Member> list() {
		return this.memberMapper.selectAllUsersInvalid();
	}

	@Override
	public void delete(int id) {
		this.memberMapper.deleteUser(id);
	}

}
