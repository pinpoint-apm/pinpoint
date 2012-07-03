package com.nhn.hippo.testweb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhn.hippo.testweb.domain.Member;
import com.nhn.hippo.testweb.repository.MemberDao;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

	@Autowired
	@Qualifier("memberDaoJdbc")
	MemberDao dao;

	public void add(Member member) {
		dao.add(member);
	}

	public void delete(int id) {
		dao.delete(id);
	}

	public Member get(int id) {
		return dao.get(id);
	}

	public List<Member> list() {
		return dao.list();
	}

	public void update(Member member) {
		dao.update(member);
	}

}
