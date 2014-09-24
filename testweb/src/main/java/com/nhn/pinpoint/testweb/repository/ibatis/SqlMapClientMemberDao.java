package com.nhn.pinpoint.testweb.repository.ibatis;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.repository.MemberDao;

/**
 * @author Hyun Jeong
 */
@Repository("sqlMapClientMemberDao")
public class SqlMapClientMemberDao implements MemberDao {

	@Autowired
	@Qualifier("mysqlSqlMapClientTemplate")
	protected SqlMapClientTemplate sqlMapClientTemplate;
	
	@Override
	public void add(Member member) {
		try {
            this.sqlMapClientTemplate.getSqlMapClient().insert("add", member);
		} catch (SQLException e) {
			throw translateSqlException("SqlMapClient add", e);
		}
	}

	@Override
	public void addStatement(Member member) {
		try {
			this.sqlMapClientTemplate.getSqlMapClient().insert("addStatement", member);
		} catch (SQLException e) {
			throw translateSqlException("SqlMapClient addStatement", e);
		}
	}

	@Override
	public void update(Member member) {
		try {
			this.sqlMapClientTemplate.getSqlMapClient().update("update", member);
		} catch (SQLException e) {
			throw translateSqlException("SqlMapClient addStatement", e);
		}
	}

	@Override
	public Member get(int id) {
		try {
			return (Member)this.sqlMapClientTemplate.getSqlMapClient().queryForObject("get", id);
		} catch (SQLException e) {
			throw translateSqlException("SqlMapClient get", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Member> list() {
		try {
			return this.sqlMapClientTemplate.getSqlMapClient().queryForList("list");
		} catch (SQLException e) {
			throw translateSqlException("SqlMapClient list", e);
		}
	}

	@Override
	public void delete(int id) {
		try {
			this.sqlMapClientTemplate.getSqlMapClient().delete("delete", id);
		} catch (SQLException e) {
			throw translateSqlException("SqlMapClient delete", e);
		}
	}
	
	private DataAccessException translateSqlException(String task, SQLException e) {
		return this.sqlMapClientTemplate.getExceptionTranslator().translate(task, null, e);
	}

}
