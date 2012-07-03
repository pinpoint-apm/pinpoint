package com.nhn.hippo.testweb.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.testweb.domain.Member;

@Repository
public class MemberDaoIbatis implements MemberDao {

	@Autowired
	SqlMapClientTemplate sqlMapClientTemplate;

	public void add(Member member) {
		sqlMapClientTemplate.insert("add", member);
	}

	public void delete(int id) {
		sqlMapClientTemplate.delete("delete", id);
	}

	public Member get(int id) {
		return (Member) sqlMapClientTemplate.queryForObject("get", id);
	}

	@SuppressWarnings("unchecked")
	public List<Member> list() {
		return sqlMapClientTemplate.queryForList("list");
	}

	public void update(Member member) {
		sqlMapClientTemplate.update("update", member);
	}

}
