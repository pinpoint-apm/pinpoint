package com.nhn.pinpoint.testweb.repository.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.testweb.domain.Member;

/**
 * @author Hyun Jeong
 */
@Repository("memberMapper")
public interface MemberMapper {
	
	public Member selectUser(@Param("id") int id);
	
	public List<Member> selectAllUsersInvalid();
	
	public int insertUser(Member member);
	
	public int updateUser(Member member);
	
	public int deleteUser(@Param("id") int id);
}
