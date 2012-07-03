package com.nhn.hippo.testweb.service;

import java.util.List;

import com.nhn.hippo.testweb.domain.Member;

public interface MemberService {

	void add(Member member);

	void update(Member member);

	Member get(int id);

	List<Member> list();

	void delete(int id);

}
