package zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import registry.Constant;

import java.util.concurrent.CountDownLatch;

/**
 * Created by homer on 17-4-10.
 */
public class Registry {
    private String address;
    private ZooKeeper zooKeeper;
    private void init() {
        try {
            zooKeeper = connectServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getConf() throws Exception{
        zooKeeper.getChildren("/conf", new Watcher() {
            public void process(WatchedEvent watchedEvent) {

            }
        });
        return "";
    }

    private ZooKeeper connectServer() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(address, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            }
        });
        latch.await();
        return zk;
    }
}
