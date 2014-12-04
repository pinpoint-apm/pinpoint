package com.nhn.pinpoint.testweb.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.testweb.domain.Member;

@Repository
public class MemberDaoIbatis implements MemberDao {

	@Autowired
	@Qualifier("mysqlSqlMapClientTemplate")
	private SqlMapClientTemplate sqlMapClientTemplate;

	public void add(Member member) {
		sqlMapClientTemplate.insert("add", member);
	}

	@Override
	public void addStatement(Member member) {
		sqlMapClientTemplate.insert("addStatement", member);
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
