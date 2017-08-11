package io.netty.example.http.controller;

import java.util.Map;

import io.netty.example.http.response.ResponseUtil;
import io.netty.example.http.template.FreeMarker;
import io.netty.handler.codec.http.FullHttpResponse;

public class IndexControllerImpl implements IController{

	@Override
	public FullHttpResponse doCtr(Map<String, String> params) {
		//渲染模板
		try {
			FreeMarker fm = new FreeMarker();
			byte[] content = fm.renderToByte("html/index.html", null);
			return ResponseUtil.responseOK(content);  
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseUtil.responseServerError("error");
		}  
	}



}
