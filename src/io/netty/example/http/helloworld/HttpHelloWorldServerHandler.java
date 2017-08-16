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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
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

	private Map<String, String> requestParams = new HashMap<>();
	private HttpPostRequestDecoder decoder;
	private boolean readingChunks;
	private boolean keepAlive;
	private HttpData partialContent;
	private HttpRequest req;
	private RouteInfo routeInfo;
	private String method;
	private FullHttpResponse response = null;
	private boolean isGet;
	private boolean isPost;
	private HttpMethod httpMethod;

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk
																												// if
																												// size
																												// exceed

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	/**
	 * uri /a/b/c/d
	 * 
	 * @param uri
	 * @throws Exception 
	 */
	private void setRouteInfo(String uri) throws Exception {
		// 获取路由信息
		QueryStringDecoder pathDecoder = new QueryStringDecoder(req.uri());// 处理URI
																			// 获取path路径
		String path = pathDecoder.path();
		
		//处理favicon.ico
		if( path.equals("/favicon.ico") ){
			routeInfo = Route.getInstance().getByUri(path);
			method = null;
			return ;
		}
			 
		String[] paths = path.split("\\.");
		if( paths.length > 2 ){
			throw new Exception("错误的请求路径");
		}
		
		if( paths.length == 1 ){
			routeInfo = Route.getInstance().getByUri(paths[0]);
			method = null;
			return ;
		}
		
		if( paths.length == 2 ){
			routeInfo = Route.getInstance().getByUri(paths[0]);
			method = paths[1];
		} 
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if (msg instanceof HttpRequest) {
			req = (HttpRequest) msg;
			keepAlive = HttpUtil.isKeepAlive(req);

			// 获取URL参数
			setParams(req.uri());
			setRouteInfo(req.uri());

			httpMethod = req.method();

			if (httpMethod.equals(HttpMethod.POST)) {
				isPost = true;
				try {
					decoder = new HttpPostRequestDecoder(factory, req);
					readingChunks = HttpUtil.isTransferEncodingChunked(req);
					if (readingChunks) {
						readingChunks = true;
					}
				} catch (ErrorDataDecoderException e1) {
					response = ResponseUtil.responseServerError("路由不存在");
				}
			} else if (httpMethod.equals(HttpMethod.GET)) {
				// GET请求不处理postRequestDecoder
				isGet = true;
				dobusiness(ctx);
			}

		}
		// Post
		if (isPost && decoder != null && msg instanceof HttpContent) {
			HttpContent chunk = (HttpContent) msg;
			try {
				decoder.offer(chunk);
			} catch (ErrorDataDecoderException e1) {
				throw e1;
			}
			readHttpDataChunkByChunk();
			if (chunk instanceof LastHttpContent) {
				readingChunks = false;
				reset();
				dobusiness(ctx);
			}
		}

		if (!isGet && !isPost) {
			ctx.write(ResponseUtil.response(HttpResponseStatus.FORBIDDEN, "Not support method " + httpMethod));
			ctx.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.write(ResponseUtil.responseServerError("服务器异常:" + cause.getMessage()));
		ctx.close();
	}

	private Map<String, String> setParams(String uri) {// 请求参数
		QueryStringDecoder decoder = new QueryStringDecoder(uri);//
		Map<String, List<String>> parame = decoder.parameters();// 解析参数
		Iterator<Entry<String, List<String>>> iterator = parame.entrySet().iterator();// iterator是迭代器
		while (iterator.hasNext()) {// 使用hasNext()检查序列中是否还有元素
			Entry<String, List<String>> next = iterator.next();// 使用next()获得序列中的下一个元素
			requestParams.put(next.getKey(), next.getValue().get(0));
		}
		return requestParams;
	} 
	private void readHttpDataChunkByChunk() throws IOException {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					if (partialContent == data) {
						partialContent = null;
					}
					try {
						writeHttpData(data);
					} finally {
						data.release();
					}
				}
			}
		} catch (EndOfDataDecoderException e1) {
			throw e1;
		}
	}

	private void writeHttpData(InterfaceHttpData data) throws IOException {
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			String value;
			try {
				value = attribute.getValue();
			} catch (IOException e1) {
				// Error while reading data from File, only print name and error
				e1.printStackTrace();
				throw e1;
			}
			requestParams.put(attribute.getName(), value);
			System.out.println(requestParams);
		} else {
			if (data.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload fileUpload = (FileUpload) data;
				if (fileUpload.isCompleted()) {
					if (fileUpload.length() < 10000) {

					} else {

					}
					fileUpload.isInMemory();// tells if the file is in Memory
					// or on File
					/*
					 * try { fileUpload.renameTo(new File("D:/test.png")); }
					 * catch (IOException e) { // TODO Auto-generated catch
					 * block e.printStackTrace(); }
					 */
					// enable to move into another
					// File dest
					// decoder.removeFileUploadFromClean(fileUpload);
					// //remove
					// the File of to delete file
				}
			}
		}
	}

	private void dobusiness(ChannelHandlerContext ctx) {
		// dobusiness
		// 处理路由
		if (routeInfo == null) {
			response = ResponseUtil.responseServerError("路由不存在");
		} else if (!routeInfo.getMethod().equalsIgnoreCase(req.method().name()) && !routeInfo.getMethod().equals("*")) {
			response = ResponseUtil.responseServerError("不支持Method：" + req.method().name());
		} else {// 处理正常业务
			try {
				IController ctr = (IController) (Class.forName(routeInfo.getClz()).newInstance());
				if (null == method) {
					response = ctr.doCtr(requestParams);
				} else {
					response = ctr.doCtr(requestParams, method);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				response = ResponseUtil.responseServerError("未找到路由实现");
			}
		}

		if (!keepAlive) {
			ctx.write(response).addListener(ChannelFutureListener.CLOSE);
		} else {
			response.headers().set(CONNECTION, KEEP_ALIVE);
			ctx.write(response);
		}
	}

	private void reset() {
		// destroy the decoder to release all resources
		decoder.destroy();
		decoder = null;
	}

}