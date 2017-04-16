import client.ZookeeperDiscovery;


/**
 * Created by homer on 17-4-3.
 */
public class Test {
    public static void main(String[] args) {
        String registryAddress = "127.0.0.1:2181";
        ZookeeperDiscovery registry = new ZookeeperDiscovery(registryAddress);
        HelloService helloService = new RpcProxy(registry).create(HelloService.class);
        String result = helloService.hello("dddd");
    }

}
