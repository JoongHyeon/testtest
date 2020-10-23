package com.kh.spring.member.service;

import java.util.HashMap;

import com.kh.spring.member.model.vo.Member;

public interface MemberService {

	Member memberLogin(Member m);

	int insertMember(Member m);

	int updateMember(Member m);

	int deleteMember(Member m);

	int updatePwd(HashMap<String, String> map);

	int checkIdDup(String id);

}
