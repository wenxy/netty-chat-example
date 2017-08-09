package io.netty.example.telnet.manager;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

//µ¥ÀýÄ£Ê½
public class ChannelManager {

	private Map<String, ChannelHandlerContext> channelMap = new HashMap<String, ChannelHandlerContext>();

	private static final ChannelManager cm = new ChannelManager();

	private ChannelManager() {
	}

	public synchronized static ChannelManager getInstance() {
		return cm;
	}

	public void add(String key, ChannelHandlerContext ctx) {
		if (null == ctx)
			return;
		channelMap.put(key, ctx);
	}

	public ChannelHandlerContext get(String key) {
		return channelMap.get(key);
	}
}
