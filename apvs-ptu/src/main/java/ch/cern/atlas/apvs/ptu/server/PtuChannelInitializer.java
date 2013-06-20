package ch.cern.atlas.apvs.ptu.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class PtuChannelInitializer extends ChannelInitializer<SocketChannel> {

	private ChannelInboundHandlerAdapter handler;

	public PtuChannelInitializer(ChannelInboundHandlerAdapter handler) {
		this.handler = handler;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(
				new IdleStateHandler(60, 30, 0),
				new DelimiterBasedFrameDecoder(8192, Unpooled
						.wrappedBuffer(new byte[] { 0x13 })),
				new StringDecoder(CharsetUtil.UTF_8),
				new StringEncoder(CharsetUtil.UTF_8),
				handler);
	}
}
