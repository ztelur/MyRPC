package zk;

import io.netty.util.internal.StringUtil;
import javafx.collections.transformation.SortedList;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by homer on 17-4-16.
 */
public class DistributeLock {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int DEFAULT_TIMEOUT_PERIOD = 10000;
    private static final String CONNECT_STRING = "127.0.0.1:2180";
    private static final byte[] data = {0x12,0x34};
    private ZooKeeper zooKeeper;
    private String root;
    private String id;
    private LockNode idName;
    private String ownerId;
    private String lastChildId;
    private Throwable other = null;
    private KeeperException exception = null;
    private InterruptedException interrupt = null;

    public DistributeLock(String root) {
        try {
            this.zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, null);
            this.root = root;
            ensureExists(root);
        } catch (IOException e) {
            e.printStackTrace();
            other  = e;
        }
    }

    public void lock() throws Exception {
        if (exception != null) {
            throw exception;
        }
        if (interrupt != null) {
            throw interrupt;
        }

        if (other != null) {
            throw new Exception("", other);
        }

        //TODO:错误的!!
        if (isOwner()) { //锁重入
            return;
        }
        BooleanMutex mutex = new BooleanMutex();
        acquireLock(mutex);
        //因为这个是watcher是异步的，所以，观察了lessThanMe之后，会立刻返回，所以在mutex上进行阻塞．
        try {
            mutex.lock();
        } catch (Exception e) {
            e.printStackTrace();
            if (! mutex.state()) {
                lock();
            }
        }
        if (exception != null) {
            throw exception;
        }
        if (interrupt != null) {
            throw interrupt;
        }

        if (other != null) {
            throw new Exception("", other);
        }
    }

    public boolean tryLock() throws Exception {
        if (exception != null) {
            throw exception;
        }

        if (isOwner()) {
            return true;
        }
        acquireLock(null);
        if (exception != null) {
            throw exception;
        }

        if (interrupt != null) {
            Thread.currentThread().interrupt();
        }

        if (other != null) {
            throw new Exception("", other);
        }

        return isOwner();
    }

    public void unlock() throws KeeperException {
        if (id != null) {
            try {
                zooKeeper.delete(root + "/" + id, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (KeeperException.NoNodeException e) {
                //do nothing
            }
        } else {
            //do nothing
        }
    }

    private void ensureExists(final String path) {
        try {
            Stat stat = zooKeeper.exists(path, false);
            if (stat != null) {
                return;
            }
            zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException e) {
            exception = e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            interrupt = e;
        }
    }

    public String getRoot() {
        return root;
    }

    public boolean isOwner() {
        return id != null && ownerId != null && id.equals(ownerId);
    }
    //返回当期节点的id
    public String getId() {
        return this.id;
    }


    private Boolean acquireLock(final BooleanMutex mutex) {
        try {
            do {
                if (id == null) { //构建当前id的唯一标识
                    long sessionId = zooKeeper.getSessionId();
                    String prefix = "x-" + sessionId + "-";
                    //如果第一次，则创建一个节点
                    String path = zooKeeper.create(root + "/" + prefix, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                    int index = path.lastIndexOf("/");
                    id = path.substring(index + 1, path.length()); //找到了index
                    idName = new LockNode(id);
                }

                if (id != null) {
                    List<String> names = zooKeeper.getChildren(root, false);
                    if (names.isEmpty()) {
                        id = null; //异常状态，重新创建一个
                    } else {
                        //对节点进行排序
                        SortedSet<LockNode> sortedNames = new TreeSet<LockNode>();
                        for (String name : names) {
                            sortedNames.add(new LockNode(name));
                        }

                        if (!sortedNames.contains(idName)) {
                            id = null;
                            continue;
                        }

                        //将第一个节点当作ownerId
                        ownerId = sortedNames.first().getName();
                        if (mutex != null && isOwner()) {//发现当前节点就是ownerId的话
                            mutex.unlock(); //直接更新状态
                        } else if (mutex == null) {
                            return isOwner();
                        }

                        SortedSet<LockNode> lessThanMe = sortedNames.headSet(idName);
                        if (! lessThanMe.isEmpty()) {
                            //关注一下排队在自己之前的最近的一个节点
                            LockNode lastChildName = lessThanMe.last();
                            lastChildId = lastChildName.getName();

                            //异步watcher处理
                            Stat stat = zooKeeper.exists(root + "/" + lastChildId, new Watcher() {
                                public void process(WatchedEvent watchedEvent) {
                                    acquireLock(mutex); //如果被提醒，那么就再次调用acquireLock
                                }
                            });

                            if (stat == null) {
                                acquireLock(mutex); //如果节点不存在，需要自己重新触发一下，watcher不会被挂上去
                            }
                        } else {
                            if (isOwner()) {
                                mutex.unlock();
                            } else {
                                id = null; //可能是自己的节点已经超时挂了，所以id和ownerid不同
                            }
                        }
                    }
                }
            } while (id == null);

        } catch (KeeperException e) {
            exception = e;
            if (mutex != null) {
                mutex.unlock();
            }
        } catch (InterruptedException e) {
            interrupt = e;
            if (mutex != null) {
                mutex.unlock();
            }
        } catch (Throwable e) {
            other = e;
            if (mutex != null) {
                mutex.unlock();
            }
        }
        if (isOwner() && mutex != null) {
            mutex.unlock();
        }
        return Boolean.FALSE;
    }

}
