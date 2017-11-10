package com.wjl.sso.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.wjl.common.pojo.TaotaoResult;
import com.wjl.common.util.CookieUtils;
import com.wjl.common.util.JsonUtils;
import com.wjl.mapper.TbUserMapper;
import com.wjl.pojo.TbUser;
import com.wjl.pojo.TbUserExample;
import com.wjl.pojo.TbUserExample.Criteria;
import com.wjl.sso.dao.JedisClient;
/**
 * 用户信息service
 * @author wujiale
 * 2017-10-31 下午3:34:46
 */
@Service
public class UserServiceImpl implements UserService{
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Value("${USERSESSION}")
	private String USERSESSION;
	@Value("${SESSIONTIME}")
	private Integer SESSIONTIME;

	@Autowired
	private TbUserMapper tbUserMapper;
	@Autowired
	private JedisClient jedisClient;
	
	@Override
	public List<TbUser> checkData(String content, Integer type) {
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		//对数据进行校验：1、2、3分别代表username、phone、email
		//用户名校验
		if (1 == type) {
			criteria.andUsernameEqualTo(content);
		//电话校验
		} else if ( 2 == type) {
			criteria.andPhoneEqualTo(content);
		//email校验
		} else {
			criteria.andEmailEqualTo(content);
		}
		//执行查询
		List<TbUser> list = tbUserMapper.selectByExample(example);
		return list;
	}
	
	@Override
	public TaotaoResult register(TbUser user) {
		user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
		Date date = new Date();
		user.setUpdated(date);
		user.setCreated(date);
		tbUserMapper.insert(user);
		return TaotaoResult.ok();
	}
	
	@Override
	public TaotaoResult login(String username, String password,HttpServletRequest request,HttpServletResponse response) {
		log.info("进入到sso的登录系统中,username:"+username+",password:"+password);
		TbUserExample example = new TbUserExample();
		example.createCriteria().andUsernameEqualTo(username);
		List<TbUser> list = tbUserMapper.selectByExample(example );
		if (list == null || list.size() <= 0) {
			log.error("用户名或密码错误");
			return TaotaoResult.build(500, "用户名或密码错误");
		}
		TbUser user = list.get(0);
		String pwd = DigestUtils.md5DigestAsHex(password.getBytes());
		if (!user.getPassword().equals(pwd)) {
			log.error("用户名或密码错误");
			return TaotaoResult.build(500, "用户名或密码错误");
		}
		log.info(username+":用户登录成功");
		//生成token
		String token = USERSESSION + UUID.randomUUID().toString();
		//保存用户之前，把用户对象中的密码清空。
		log.info("开始生成用户token");
		user.setPassword(null);
		//把用户信息写入redis
		jedisClient.set(token, JsonUtils.objectToJson(user));
		//设置session的过期时间
		jedisClient.expire(token, SESSIONTIME);
		//添加cookie的逻辑 默认cookie有效期是关闭浏览器就失效
		CookieUtils.setCookie(request, response, "TT_TOKEN",token);
		log.info("将token:"+token+"写入到redis中");
		//返回token
		return TaotaoResult.ok(token);
	}

	@Override
	public TaotaoResult getUserByToken(String token) {
		log.info("进入到sso的校验登录系统中,cookies中的token:"+token);
		token = token.replace("\"", "").trim();
		log.info("处理后的token:"+token);
		String json = jedisClient.get(token);
		log.info("根据token查询了redis,得到的结果是:"+json);
		if (StringUtils.isNotBlank(json)) {
			jedisClient.expire(token, SESSIONTIME);
			return TaotaoResult.ok(JsonUtils.jsonToPojo(json, TbUser.class));
		}
		log.error("session过期，请重新登录");
		return TaotaoResult.build(400, "session过期，请重新登录");
	}

	@Override
	public TaotaoResult logout(String token) {
		jedisClient.del(token);
		return TaotaoResult.ok();
	}
}
