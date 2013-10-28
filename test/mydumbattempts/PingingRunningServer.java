package mydumbattempts;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.ChannelHandler;

public class PingingRunningServer {
	
	
	public static void main(String args[]){
		 ClientBootstrap bootstrap= new ClientBootstrap(
				 new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newFixedThreadPool(2)));
		 
		 bootstrap.setOption("remoteAddress", new InetSocketAddress("localhost", 6100));
		 bootstrap.setOption("tcpNoDelay", true);
		 bootstrap.setOption("receiveBufferSize", 1048576);
		 ChannelFuture ch=bootstrap.connect();
		 ch.awaitUninterruptibly();
	}
}
