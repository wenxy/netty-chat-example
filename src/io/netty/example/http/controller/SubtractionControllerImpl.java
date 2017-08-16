package io.netty.example.http.controller;

import java.util.Map;

import io.netty.example.http.controller.dto.SubtractionDto;
import io.netty.example.http.response.ResponseUtil;
import io.netty.example.http.template.FreeMarker;
import io.netty.handler.codec.http.FullHttpResponse;

public class SubtractionControllerImpl extends AbstractController {

	@Override
	public FullHttpResponse doCtr(Map<String, Object> params) {
		
		if( !params.containsKey("firstNumber") || !params.containsKey("secNumber") ){
			return ResponseUtil.responseServerError("参数错误");
		}
		
		try{
			SubtractionDto dto = new SubtractionDto();
			
			//处理业务逻辑，或者更复杂的逻辑
			double firstNumber = Double.parseDouble(params.get("firstNumber").toString());
			double secNumber =  Double.parseDouble(params.get("secNumber").toString());
			dto.setResult(firstNumber - secNumber);
			dto.setDesc("计算结果:"+firstNumber +"-"+secNumber+"=");
			
			
			//渲染模板
			FreeMarker fm = new FreeMarker();
			/*Map<String,Object> args = new HashMap();
			args.put("root",dto);
			args.put("desc","测试");
			args.put("result","测试");*/
			byte[] content = fm.renderToByte("html/result/result.html", dto);
			
			return ResponseUtil.responseOK(content); 
		}catch(Exception e){
			e.printStackTrace();
			return ResponseUtil.responseServerError("服务器异常："+e.getMessage());
		}
	}

}
