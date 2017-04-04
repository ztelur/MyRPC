import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

/**
 * Created by homer on 17-4-3.
 */
public class Test {
    public static void main(String[] args) {
        HelloService helloService = new RpcProxy("127.0.0.1:9092").create(HelloService.class);
        String result = helloService.hello("dddd");

        System.out.println("hello" + result);
    }
    private static void test() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        String host = "127.0.0.1";
        int port = 9092;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("the host is " + host + " " + port);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().write(new String("ddddd")).sync();
        } finally {
            group.shutdown();
        }
    }
}
