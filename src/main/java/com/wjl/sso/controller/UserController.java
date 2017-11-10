package com.wjl.sso.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wjl.common.pojo.TaotaoResult;
import com.wjl.common.util.ExceptionUtil;
import com.wjl.pojo.TbUser;
import com.wjl.sso.service.UserService;

/**
 * 校验用户注册信息@Controller
 * @author wujiale
 * 2017-10-31 下午4:03:57
 */
@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	
	@RequestMapping("/check/{content}/{type}")
	@ResponseBody
	public Object checkData(@PathVariable String content,@PathVariable Integer type,String callback){
		//1 被占用 2可以使用
		TaotaoResult taotaoResult = null;
		try {
			List<TbUser> list = userService.checkData(content, type);
			if (list != null && list.size() > 0) {
				return "1";
			}
			return "2";
		} catch (Exception e) {
			e.printStackTrace();
			taotaoResult = TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		if (StringUtils.isNotBlank(callback)) {
			taotaoResult = new TaotaoResult(true);
			MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(taotaoResult);
			mappingJacksonValue.setJsonpFunction(callback);
			return mappingJacksonValue;
		} else {
			return taotaoResult; 
		}
	}
	
	@RequestMapping(value="/register",method=RequestMethod.POST)
	@ResponseBody
	public TaotaoResult register(TbUser user,HttpServletResponse response){
		try {
			response.sendRedirect("/page/login");
			return userService.register(user);
		} catch (Exception e) {
			e.printStackTrace();
			return TaotaoResult.ok(new String("提交的值被占用"));
		}
	}
	
	@RequestMapping("/login")
	@ResponseBody
	public TaotaoResult login(String username, String password,HttpServletRequest request,HttpServletResponse response) {
		try {
			TaotaoResult result = userService.login(username, password,request,response);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
		}
	}
	
	@RequestMapping("/logout/{token}")
	@ResponseBody
	public TaotaoResult logout(@PathVariable String token,HttpServletRequest request){
//System.out.println("sso:"+token);
		return userService.logout(token);
	}
	
	@RequestMapping("/token/{token}")
	@ResponseBody
	public Object getUserByToken(@PathVariable String token,String callback) {
		TaotaoResult result = null;
		try {
			result = userService.getUserByToken(token);
		} catch (Exception e) {
			e.printStackTrace();
			result = TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		if (StringUtils.isNotBlank(callback)) {
			MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
			mappingJacksonValue.setJsonpFunction(callback);
			return mappingJacksonValue;
		} else {
			return result; 
		}
	}
	
//	@RequestMapping("/check/{content}/{type}")
//	public void test(@PathVariable String content,@PathVariable Integer type,HttpServletResponse response) throws IOException{
//		response.setContentType("text/html,charset=utf-8");
//		PrintWriter writer = response.getWriter();
//		List<TbUser> list = userService.checkData(content, type);
//		if (list != null && list.size() > 0) {
//			writer.write("sorry");
//		}else{
//			writer.write("success");
//		}
//	}
	
}
