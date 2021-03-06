package ch.cern.atlas.apvs.ptu.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PtuPullServer {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	private final int port;
	private final int refresh;
	private final String[] ids;

	public PtuPullServer(int port, int refresh, String[] ids) {
		this.refresh = refresh;
		this.port = port;
		this.ids = ids;
	}

	public void run() {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap();
		try {
			
			EventLoopGroup group = new NioEventLoopGroup();
			
			bootstrap.group(group);
			bootstrap.channel(NioServerSocketChannel.class);

			// Set up the pipeline factory.
			bootstrap.childHandler(new PtuChannelInitializer(new PtuServerHandler(refresh, ids), null, true));

			// Bind and start to accept incoming connections.
			ChannelFuture f = bootstrap.bind(new InetSocketAddress(port))
					.sync();
			log.info("PTU Pull Server open at " + port);

			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			log.warn("Problem: "+e);
		} finally {
			// Netty 4, no more need to release anything or shutdown ?
		}
	}

	public static void main(String[] args) {
		new PtuPullServer(4005, 5000, null).run();
	}
}