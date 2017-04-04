package protocol;


import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by homer on 17-4-2.
 */
public class SerializationUtil {
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
    private static Objenesis objenesis = new ObjenesisStd(true);

    public static <T> T deserialize(byte[] data, Class<T> genericClass) {
        try {
            T message = (T) objenesis.newInstance(genericClass);
            Schema<T> schema = getSchema(genericClass);
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    public static <T> byte[] serialize(T object) {
        Class<T> cls = (Class<T>) object.getClass();
        System.out.println(cls.getName());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtobufIOUtil.toByteArray(object, schema, buffer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }



    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            System.out.println("the schema is null");
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                System.out.println("the schema created");
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }



}
