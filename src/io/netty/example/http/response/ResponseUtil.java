package io.netty.example.http.response;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;

/**
 * 定义response，避免重复写代码
 * @author fish
 *
 */
public class ResponseUtil {

	private static final String charset = "utf-8";
	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
	
	public static FullHttpResponse response(HttpResponseStatus status,String message){
		FullHttpResponse response = null;
		try {
			response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.wrappedBuffer(message.getBytes(charset)));
			response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
			response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
			 
		} catch (UnsupportedEncodingException e) { 
			response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.wrappedBuffer("UnsupportedEncodingException".getBytes()));
			response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
			response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
		}
		return response;
		
	}
	
	public static FullHttpResponse response(HttpResponseStatus status,byte[] message){
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.wrappedBuffer(message));
		response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
		response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
		return response;
	}
	
	public static FullHttpResponse responseOK(String message){ 
		return response(OK,message);
	}
	
	public static FullHttpResponse responseOK(byte[]  message){ 
		return response(OK,message);
	}
	
	public static FullHttpResponse responseServerError(String message){ 
		return response(INTERNAL_SERVER_ERROR,message);
	}
	
	public static FullHttpResponse responseServerError(byte[]  message){ 
		return response(INTERNAL_SERVER_ERROR,message);
	} 
}
