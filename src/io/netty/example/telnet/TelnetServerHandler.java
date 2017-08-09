package io.netty.example.telnet;

import java.net.InetAddress;
import java.util.Date;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.telnet.manager.ChannelManager;

public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Send greeting for a new connection.
		ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
		ctx.write("It is " + new Date() + " now.\r\n");
		ctx.flush();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
		// Generate and write a response.
		String response;
		boolean close = false;
		if (request.isEmpty()) {
			response = "Please type something.\r\n";
		} 
		String[] clientInfo = request.split(":");
		String cmd = clientInfo[0];
		if("bye".equals(cmd)){
			response = "Have a good day!\r\n";
			ctx.writeAndFlush("bye "+response);
			close = true;
		}else if("init".equals(cmd)){
			String fromId = clientInfo[1];
			ChannelManager.getInstance().add(fromId, ctx);
			ctx.writeAndFlush(cmd+","+fromId+",success\r\n");
		}else if("send".equals(cmd)){
			String toId = clientInfo[1];
			String msg = clientInfo[2]; 
			ChannelHandlerContext toCtx = ChannelManager.getInstance().get(toId);
			if(toCtx.isRemoved()){
				ctx.writeAndFlush(cmd+","+toId+",fail,is removed."+",msg="+msg+"\r\n");
			}else{
				toCtx.writeAndFlush(msg+"\r\n");
				ctx.writeAndFlush(cmd+","+toId+",success"+",msg="+msg+"\r\n");
			}
		} 

		// We do not need to write a ChannelBuffer here.
		// We know the encoder inserted at TelnetPipelineFactory will do the
		// conversion.
		

		// Close the connection after sending 'Have a good day!'
		// if the client has sent 'bye'.
		/*if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}*/
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
