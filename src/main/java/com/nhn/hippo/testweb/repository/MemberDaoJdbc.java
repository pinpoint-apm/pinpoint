package com.nhn.hippo.testweb.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.testweb.domain.Member;

@Repository
public class MemberDaoJdbc implements MemberDao {

	@Autowired
	SimpleJdbcTemplate jdbcTemplate;

	public void setMemberMapper(RowMapper<Member> memberMapper) {
		this.memberMapper = memberMapper;
	}

	RowMapper<Member> memberMapper = new RowMapper<Member>() {
		public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
			Member member = new Member();
			member.setId(rs.getInt("id"));
			member.setName(rs.getString("name"));
			member.setJoined(rs.getDate("joined"));
			return member;
		}
	};

	public void add(Member member) {
		jdbcTemplate.update(
				"insert into member(id, name, joined) values (?, ?, ?)",
				member.getId(), member.getName(), member.getJoined());
	}

	public void delete(int id) {
		jdbcTemplate.update("delete from member where id = ?", id);
	}

	public Member get(int id) {
		return jdbcTemplate.queryForObject("select * from member where id = ?",
				memberMapper, id);
	}

	public List<Member> list() {
		return jdbcTemplate.query("select * from member", memberMapper);
	}

	public void update(Member member) {
		jdbcTemplate
				.update("update member set name = :name, joined = :joined where id = :id",
						new BeanPropertySqlParameterSource(member));
	}
}
