package com.nhn.hippo.testweb.repository;

import java.util.List;

import com.nhn.hippo.testweb.domain.Member;

public interface MemberDao {

	void add(Member member);

	void update(Member member);

	Member get(int id);

	List<Member> list();

	void delete(int id);

}
