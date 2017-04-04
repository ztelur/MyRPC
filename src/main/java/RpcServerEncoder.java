import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import protocol.SerializationUtil;

/**
 * Created by homer on 17-4-4.
 */
public class RpcServerEncoder extends MessageToByteEncoder<RpcResponse> {
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        byte[] data = SerializationUtil.serialize(rpcResponse);
        System.out.println("add count " + data);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
