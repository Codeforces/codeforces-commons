package com.codeforces.commons.process;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 21.02.13
 */
public class ReadWriteEvent {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition condition = lock.writeLock().newCondition();

    public Lock getReadLock() {
        return lock.readLock();
    }

    public Lock getWriteLock() {
        return lock.writeLock();
    }

    public Condition getCondition() {
        return condition;
    }

    public void await(long intervalMillis) {
        try {
            condition.await(intervalMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            // No operations.
        }
    }

    public void signalAll() {
        condition.signalAll();
    }
}
