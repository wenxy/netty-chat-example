package io.netty.example.http.controller;

import java.io.File;
import java.util.Map;

import io.netty.example.http.controller.dto.AdditionDto;
import io.netty.example.http.response.ResponseUtil;
import io.netty.example.http.template.FreeMarker;
import io.netty.handler.codec.http.FullHttpResponse;

public class CalController extends AbstractController {

	public FullHttpResponse add(Map<String,String> params){
		
		if( !params.containsKey("firstNumber") || !params.containsKey("secNumber") ){
			return ResponseUtil.responseServerError("参数错误");
		}
		
		try{
			AdditionDto dto = new AdditionDto();
			
			//处理业务逻辑，或者更复杂的逻辑
			double firstNumber = Double.parseDouble(params.get("firstNumber"));
			double secNumber =  Double.parseDouble(params.get("secNumber"));
			dto.setResult(firstNumber + secNumber);
			dto.setDesc("计算结果:"+firstNumber +"+"+secNumber+"=");
			
			
			//渲染模板
			FreeMarker fm = new FreeMarker();
			byte[] content = fm.renderToByte("html/result/result.html", dto);
			
			return ResponseUtil.responseOK(content); 
		}catch(Exception e){
			e.printStackTrace();
			return ResponseUtil.responseServerError("服务器异常："+e.getMessage());
		}
	}
	
	public FullHttpResponse uploadFile(Map<String,Object> params){
		
		try{
			AdditionDto dto = new AdditionDto(); 
			System.out.println("打印出来参数了："+params);
			//渲染模板
			FreeMarker fm = new FreeMarker();
			byte[] content = fm.renderToByte("html/result/result.html", dto);
			
			return ResponseUtil.responseOK(content); 
		}catch(Exception e){
			e.printStackTrace();
			return ResponseUtil.responseServerError("服务器异常："+e.getMessage());
		}
	}
}
