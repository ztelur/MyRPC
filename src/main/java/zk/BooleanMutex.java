package zk;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by homer on 17-4-16.
 */
public class BooleanMutex {
    private Sync sync;

    public BooleanMutex() {
        sync = new Sync();
        set(false);
    }

    /**
     * 阻塞等待Boolean为true
     * @throws InterruptedException
     */
    public void lock() throws InterruptedException {
        sync.innerLock();
    }

    public void lockTimeOut(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        sync.innerLock(unit.toNanos(timeout));
    }

    public void unlock() {
        set(true);
    }

    public void set(Boolean mutex) {
        if (mutex) {
            sync.innerSetTrue();
        } else {
            sync.innerSetFalse();
        }
    }

    public boolean state() {
        return sync.innerState();
    }


    private final class Sync extends AbstractQueuedSynchronizer {
        // 状态为1,则唤醒被阻塞的状态为FALSE的所有线程
        private static final int TRUE = 1;
        // 状态值为0,则阻塞当前线程，等待唤醒
        private static final int FALSE = 0;
        //返回值大于0,则执行，返回值小于0,则阻塞
        @Override
        protected int tryAcquireShared(int arg) {
            return getState() == 1 ? 1 : -1; //默认值为0,所以先上来会阻塞
        }
        //释放贡献锁的判断
        @Override
        protected boolean tryReleaseShared(int arg) {
            return true;
        }

        private boolean innerState() {
            return getState() == 1;
        }

        private void innerLock() throws InterruptedException {
            acquireSharedInterruptibly(0);
        }

        private void innerLock(long nanosTimeout) throws InterruptedException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout)) {
                throw new TimeoutException();
            }
        }

        private void innerSetTrue() {
            for (;;) {
                int s = getState();
                if (s == TRUE) {
                    return;
                }
                if (compareAndSetState(s, TRUE)) { //cas更新状态，避免并发更新true操作
                    releaseShared(0); // 释放锁对象，唤醒一下阻塞的Thread
                }
            }
        }

        private void innerSetFalse() {
            for (;;) {
                int s = getState();
                if (s == FALSE) {
                    return;
                }
                if (compareAndSetState(s, FALSE)) {
                    setState(FALSE);
                }
            }
        }
    }
}
