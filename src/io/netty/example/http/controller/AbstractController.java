package io.netty.example.http.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import io.netty.example.http.response.ResponseUtil;
import io.netty.handler.codec.http.FullHttpResponse;

public abstract class AbstractController implements IController{
	
	@Override
	public FullHttpResponse doCtr(Map<String, String> params) {
		return null;
	}
	
	@Override
	public FullHttpResponse doCtr(Map<String, String> params,String method) {
		try {
			Class clazz = this.getClass();
			Method m = clazz.getDeclaredMethod(method, Map.class);
			FullHttpResponse result = (FullHttpResponse)m.invoke(this, params);
			return result;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return ResponseUtil.responseServerError("服务器异常，方法不存在："+method);
		} 
	}
}
