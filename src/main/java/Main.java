import registry.ServiceRegistry;

/**
 * Created by homer on 17-4-2.
 */
public class Main {
    public static void main(String[] args) {
        String registryAddress = "127.0.0.1:2181";
        ServiceRegistry registry = new ServiceRegistry(registryAddress);
        registry.register("127.0.0.1:9094");
        RpcServer server = new RpcServer("127.0.0.1:9094",registry);
        try {
            server.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
