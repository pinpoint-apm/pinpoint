package com.nhn.pinpoint.testweb.repository;

import java.util.List;

import com.nhn.pinpoint.testweb.domain.Member;

public interface MemberDao {

	void add(Member member);

    void addStatement(Member member);

	void update(Member member);

	Member get(int id);

	List<Member> list();

	void delete(int id);

}
