package io.netty.example.http.controller;

import java.util.Map;

import io.netty.example.http.response.ResponseUtil;
import io.netty.handler.codec.http.FullHttpResponse;

public class FaviconControllerImpl implements IController{

	@Override
	public FullHttpResponse doCtr(Map<String, String> params) {
		return ResponseUtil.responseOK("Ok");
	}


}
