package com.nhn.pinpoint.testweb.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nhn.pinpoint.testweb.domain.Member;
import com.nhn.pinpoint.testweb.service.MemberService;

/**
 * @author Hyun Jeong
 */
@Controller
public class OrmController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String IBATIS_VIEW = "orm/ibatis";
	public static final String MYBATIS_VIEW = "orm/mybatis";
	
	@Autowired
	@Qualifier("sqlMapClientMemberService")
	private MemberService sqlMapClientMemberService;
	
	@Autowired
	@Qualifier("sqlMapSessionMemberService")
	private MemberService sqlMapSessionMemberService;
	
	@Autowired
	@Qualifier("myBatisMemberService")
	private MemberService myBatisMemberService;
	
	@RequestMapping(value = "/orm/ibatis/sqlMapClient/query")
	public String iBatisSqlMapClientQuery(Model model) {
		logger.info("/orm/ibatis/sqlMapClient/query");
		
		this.sqlMapClientMemberService.get(0);
		
		return IBATIS_VIEW;
	}
	
	@RequestMapping(value = "/orm/ibatis/sqlMapClient/transaction")
	public String iBatisSqlMapClientTranscation(Model model) {
		logger.info("/orm/ibatis/sqlMapClient/transaction - add, update, get, delete");
		
		runTransaction(this.sqlMapClientMemberService);
		
		return IBATIS_VIEW;
	}
	
	@RequestMapping(value = "/orm/ibatis/sqlMapSession/query")
	public String iBatisSqlMapSessionQuery(Model model) {
		logger.info("/orm/ibatis/sqlMapSession/query");
		
		this.sqlMapSessionMemberService.get(0);
		
		return IBATIS_VIEW;
	}
	
	@RequestMapping(value = "/orm/ibatis/sqlMapSession/transaction")
	public String iBatisSqlMapSessionTransaction(Model model) {
		logger.info("/orm/ibatis/sqlMapSession/transaction - add, update, get, delete");
		
		runTransaction(this.sqlMapSessionMemberService);
		
		return IBATIS_VIEW;
	}
	
	@RequestMapping(value = "/orm/mybatis/sqlSessionTemplate/query")
	public String myBatisSqlSessionTemplateQuery(Model model) {
		logger.info("/orm/mybatis/sqlSessionTemplate/query");
		
		this.myBatisMemberService.get(0);
		
		return MYBATIS_VIEW;
	}
	
	@RequestMapping(value = "/orm/mybatis/sqlSessionTemplate/transaction")
	public String myBatisSqlSessionTemplateTransaction(Model model) {
		logger.info("/orm/mybatis/sqlSessionTemplate/transaction");
		
		runTransaction(this.myBatisMemberService);
		
		return MYBATIS_VIEW;
	}
	
	@RequestMapping(value = "/orm/mybatis/sqlSessionTemplate/invalid")
	public String myBatisSqlSessionTemplateInvalid(Model model) {
		logger.info("/orm/mybatis/sqlSessionTemplate/invalid");
		
		this.myBatisMemberService.list();
		
		return MYBATIS_VIEW;
	}
	
	private void runTransaction(MemberService memberService) {
		
		final int memberId = 1574;

		Member member = new Member();
		member.setId(memberId);
		member.setName("test User");
		member.setJoined(new Date(System.currentTimeMillis()));
		
		memberService.add(member);
		
		member.setName("updated test User");
		memberService.update(member);
		
		memberService.get(memberId);
		logger.info("\tId:[" + member.getId() + "], name:[" + member.getName() + "]");
		
		memberService.delete(memberId);
	}
}
