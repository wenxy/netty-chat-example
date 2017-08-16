package io.netty.example.http.helloworld;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.AsciiString;

public class HttpHelloWorldServerHandler extends ChannelInboundHandlerAdapter {
	 
	private static final AsciiString CONNECTION = new AsciiString("Connection");
	private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

	private Map<String, Object> requestParams = new HashMap<>();
	
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
																												// exceed

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
                                                         // on exit (in normal
                                                         // exit)
        DiskFileUpload.baseDirectory =  null; // system temp directory
         
    	DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
                                                        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
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
	
	/**
	 * uri /a/b/c.d
	 * 其中 /a/b/c 指定其实现类路由
	 * d指定路由实现类的方法
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
	

	private void setParams(String uri) {// 请求参数
		QueryStringDecoder decoder = new QueryStringDecoder(uri);//
		Map<String, List<String>> parame = decoder.parameters();// 解析参数
		Iterator<Entry<String, List<String>>> iterator = parame.entrySet().iterator();// iterator是迭代器
		while (iterator.hasNext()) {// 使用hasNext()检查序列中是否还有元素
			Entry<String, List<String>> next = iterator.next();// 使用next()获得序列中的下一个元素
			requestParams.put(next.getKey(), next.getValue().get(0));
		} 
	} 
	
	private void readHttpDataChunkByChunk() throws Exception {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					if (partialContent == data) {
						System.out.println(" 100% (FinalSize: " + partialContent.length() + ")");
						partialContent = null;
					}
					try {
						writeHttpData(data);
					} finally {
						data.release();
					}
				}
			}
			// Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        builder.append("Start FileUpload: ")
                            .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        builder.append("Start Attribute: ")
                            .append(partialContent.getName()).append(" ");
                    }
                    builder.append("(DefinedSize: ").append(partialContent.definedLength()).append(")");
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                        .append("% "); 
                } else {
                    builder.append(" ").append(partialContent.length()).append(" "); 
                }
                System.out.println(builder.toString());
            }
		} catch (EndOfDataDecoderException e1) {
			System.out.println("EndOfDataDecoderException");
		}
	}

	private void writeHttpData(InterfaceHttpData data) throws Exception {
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
		} else {
			if (data.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload fileUpload = (FileUpload) data;
				String filename = fileUpload.getFilename();
				if (fileUpload.isCompleted() && filename.contains(".")) { 
					String suffix = filename.substring(filename.indexOf("."));
					String newfilename = UUID.randomUUID().toString()+suffix;
					File dir =  new File(System.getProperty("user.dir")+"/tmp");
					if( !dir.exists() ){
						if(!dir.mkdirs()){
							throw new Exception("创建临时目录失败");
						}
					}
					File file = new File(System.getProperty("user.dir")+"/tmp/"+newfilename);
					
					fileUpload.renameTo(file);
					requestParams.put(data.getName(), file);
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