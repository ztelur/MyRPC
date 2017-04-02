import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import protocol.SerializationUtil;

import java.util.List;

/**
 * Created by homer on 17-4-2.
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> genericsClass;

    protected RpcDecoder(Class<?> genericsClass) {
        super();
        this.genericsClass = genericsClass;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        Object obj = SerializationUtil.deserialize(data, genericsClass);
        list.add(obj);
    }
}
