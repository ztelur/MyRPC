import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import protocol.SerializationUtil;

/**
 * Created by homer on 17-4-3.
 */
public class RpcClientEncoder extends MessageToByteEncoder<RpcRequest>{
    public RpcClientEncoder() {
        super();
    }
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest request, ByteBuf byteBuf) throws Exception {
        byte[] data = SerializationUtil.serialize(request);
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }

}
