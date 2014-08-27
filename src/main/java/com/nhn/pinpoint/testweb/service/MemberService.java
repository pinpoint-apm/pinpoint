package com.nhn.pinpoint.testweb.service;

import java.util.List;

import com.nhn.pinpoint.testweb.domain.Member;

public interface MemberService {

    void add(Member member);

    void addStatement(Member member);

	void update(Member member);

	Member get(int id);

	List<Member> list();

	void delete(int id);


}
