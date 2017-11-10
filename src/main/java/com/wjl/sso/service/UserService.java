package com.wjl.sso.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wjl.common.pojo.TaotaoResult;
import com.wjl.pojo.TbUser;

public interface UserService {
	List<TbUser> checkData(String content,Integer type);
	TaotaoResult register(TbUser user);
	TaotaoResult login(String username,String password,HttpServletRequest request,HttpServletResponse response);
	TaotaoResult logout(String token);
	TaotaoResult getUserByToken(String token);
}
