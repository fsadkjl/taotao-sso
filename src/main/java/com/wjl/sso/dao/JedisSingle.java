package com.wjl.sso.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisSingle implements JedisClient{
	private static final Logger log = LoggerFactory.getLogger(JedisSingle.class);
	
	@Autowired(required=false)
	private JedisPool jedisPool;
	
	@Override
	public String get(String key) {
		Jedis jedis = jedisPool.getResource();
		String string = jedis.get(key);
		getkeyLog(key);
		log.info("get:"+string);
		jedis.close();
		return string;
	}

	@Override
	public String set(String key, String value) {
		Jedis jedis = jedisPool.getResource();
		String string = jedis.set(key, value);
		setkeyLog(key);
		log.info("set:"+string);
		jedis.close();
		return string;
	}

	@Override
	public String hget(String hkey, String key) {
		Jedis jedis = jedisPool.getResource();
		String string = jedis.hget(hkey, key);
		getkeyLog(hkey);
		log.info("hget:"+string);
		jedis.close();
		return string;
	}

	@Override
	public long hset(String hkey, String key, String value) {
		Jedis jedis = jedisPool.getResource();
		Long result = jedis.hset(hkey, key, value);
		setkeyLog(hkey);
		log.info("hset:", result);
		jedis.close();
		return result;
	}

	@Override
	public long incr(String key) {
		Jedis jedis = jedisPool.getResource();
		Long result = jedis.incr(key);
		jedis.close();
		return result;
	}

	@Override
	public long expire(String key, int second) {
		Jedis jedis = jedisPool.getResource();
		Long result = jedis.expire(key, second);
		jedis.close();
		return result;
	}

	@Override
	public long ttl(String key) {
		Jedis jedis = jedisPool.getResource();
		Long result = jedis.ttl(key);
		jedis.close();
		return result;
	}

	@Override
	public long del(String key) {
		Jedis jedis = jedisPool.getResource();
		Long result = jedis.del(key);
		log.info("del:"+result);
		jedis.close();
		return result;
	}

	@Override
	public long hdel(String hkey, String key) {
		Jedis jedis = jedisPool.getResource();
		Long result = jedis.hdel(hkey,key);
		log.info("hdel:"+result);
		jedis.close();
		return result;
	}
	
	private void getkeyLog(String key){
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		log.info(stackTrace[1].getClassName()+":"+stackTrace[1].getMethodName()
				+"调用了get("+key+")");
	}
	
	private void setkeyLog(String key){
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		log.info(stackTrace[1].getClassName()+":"+stackTrace[1].getMethodName()
				+"调用了set("+key+")");
	}
	

}
