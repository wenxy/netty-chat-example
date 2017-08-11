package io.netty.example.http.helloworld;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.example.http.controller.IController;
import io.netty.example.http.response.ResponseUtil;
import io.netty.example.http.route.Route;
import io.netty.example.http.route.RouteInfo;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.AsciiString;

public class HttpHelloWorldServerHandler extends ChannelInboundHandlerAdapter {
	// private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ',
	// 'W', 'o', 'r', 'l', 'd' };
	// String filePath =
	// "D:\\Java\\eclipse\\io.netty.example.http.helloworld\\html\\test.html";

	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
	private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
	private static final AsciiString CONNECTION = new AsciiString("Connection");
	private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		if (msg instanceof HttpRequest) {
			
			HttpRequest req = (HttpRequest) msg;			
			//获取URL参数
			Map<String, String> params = requestParams(req);
			
			//处理URI 获取path路径
			QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
			String path = decoder.path();
			
			//获取路由信息
			RouteInfo ri = Route.getInstance().getByUri(path);
			
			FullHttpResponse response = null; 
			//处理路由
			if( ri == null ){
				response = ResponseUtil.responseServerError("路由不存在"); 
			}else if( !ri.getMethod().equalsIgnoreCase(req.method().name())  && !ri.getMethod().equals("*")){
				response = ResponseUtil.responseServerError("不支持Method："+req.method().name()); 
			}else{//处理正常业务
				try {
					//为什么这么做呢？ 解耦业务
					/*Class<?> CalculateController = Class.forName("io.netty.example.http.controller");
					
			        Method method = CalculateController.getMethod("AdoCtr");
			        response = (FullHttpResponse) method.invoke(CalculateController.newInstance());*/
			       // response = clazz.AdoCtr(params);
					
				
					IController ctr = (IController)(Class.forName(ri.getClz()).newInstance());
					response = ctr.doCtr(params);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					response = ResponseUtil.responseServerError("未找到路由实现"); 
				}
			}
  
			boolean keepAlive = HttpUtil.isKeepAlive(req); 
			if (!keepAlive) {
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				response.headers().set(CONNECTION, KEEP_ALIVE);
				ctx.write(response);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
	private Map<String, String> requestParams(HttpRequest req){//请求参数
		String uri = req.uri();
		Map<String, String> requestParams = new HashMap<>();
		// 处理get请求,get请求一般用于获取、查询资源
		if (req.method() == HttpMethod.GET) {
			QueryStringDecoder decoder = new QueryStringDecoder(uri);//
			Map<String, List<String>> parame = decoder.parameters();//解析参数
			Iterator<Entry<String, List<String>>> iterator = parame.entrySet().iterator();//iterator是迭代器
			while (iterator.hasNext()) {//使用hasNext()检查序列中是否还有元素
				Entry<String, List<String>> next = iterator.next();//使用next()获得序列中的下一个元素
				requestParams.put(next.getKey(), next.getValue().get(0));
			}
		}
		// 处理POST请求,post请求一般用于更新资源信息
		if (req.method() == HttpMethod.POST) {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
			List<InterfaceHttpData> postData = decoder.getBodyHttpDatas(); //
			for (InterfaceHttpData data : postData) {
				if (data.getHttpDataType() == HttpDataType.Attribute) {
					MemoryAttribute attribute = (MemoryAttribute) data;
					requestParams.put(attribute.getName(), attribute.getValue());
				}
			}
		} 
		return requestParams;
	}
}