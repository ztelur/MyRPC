/**
 * Created by homer on 17-4-3.
 */
public class Test {
    public static void main(String[] args) {
        HelloService helloService = new RpcProxy().create(HelloService.class);
        String result = helloService.hello("dddd");
        System.out.println("hello");
    }
}
