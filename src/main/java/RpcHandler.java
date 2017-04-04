import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by homer on 17-4-3.
 */
public class RpcHandler extends ChannelInboundMessageHandlerAdapter<RpcRequest>{
    private static final Logger LOGGER = Logger.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;
    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        System.out.println("messageReceived");
        try {
            Object result = handle(rpcRequest);
            response.setResult(result);
        } catch (Throwable e) {
            response.setError(e);
        }
        channelHandlerContext.write(response).addListener(ChannelFutureListener.CLOSE);
        channelHandlerContext.flush();
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //可以使用cglib来进行反射调用
        Method serviceMethod = serviceClass.getMethod(methodName, parameterTypes);
        serviceMethod.setAccessible(true);
        return serviceMethod.invoke(serviceBean, parameters);
    }
}
