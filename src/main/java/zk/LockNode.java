package zk;

/**
 * Created by homer on 17-4-16.
 */
public class LockNode {
    private String id;

    public LockNode(String id) {
        this.id = id;
    }
    public String getName() {
        return this.id;
    }
}
