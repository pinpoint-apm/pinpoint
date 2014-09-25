package com.nhn.pinpoint.testweb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.repository.MemberDao;

@Service("memberService")
@Transactional("mysqlTransactionManager")
public class MemberServiceImpl implements MemberService {

	@Autowired
	@Qualifier("memberDaoJdbc")
	private MemberDao dao;

    public MemberDao getDao() {
        return dao;
    }

    public void setDao(MemberDao dao) {
        this.dao = dao;
    }

    public void add(Member member) {
		dao.add(member);
	}

    @Override
    public void addStatement(Member member) {
        dao.addStatement(member);
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
