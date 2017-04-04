import com.sun.glass.ui.EventLoop;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import registry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by homer on 17-4-2.
 *
 * based on Netty , server stub
 */
public class RpcServer {
    private static final Logger LOGGER = Logger.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    //存放接口名称和服务
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    public RpcServer(String serverAddress, ServiceRegistry registry) {
        super();
        this.serverAddress = serverAddress;
        this.serviceRegistry = registry;
        this.handlerMap.put("HelloService", new HelloServiceImpl());
    }

    public void init() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcServerEncoder())
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            System.out.println("the rpc server address is " + host + " " + port);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdown();
            bossGroup.shutdown();
        }
    }
}
