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
        System.out.println("use decoder");
        this.genericsClass = genericsClass;
    }

    protected Object decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        System.out.println("decode a request");
        if (byteBuf.readableBytes() < 4) {
            System.out.println(" < 4");
            return null;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            System.out.println("close bytebuf because close");
            channelHandlerContext.close();
        }
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return null;
        }
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        System.out.println("produce a object");
        Object obj = SerializationUtil.deserialize(data, genericsClass);
        System.out.println("finish");
        if (obj == null) {
            System.out.println("the obj is null");
        }
        return obj;
    }

}
