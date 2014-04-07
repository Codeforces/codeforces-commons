package com.codeforces.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.04.14
 */
@SuppressWarnings("RefusedBequest")
public final class CountingInputStream extends InputStream {
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong totalReadByteCount = new AtomicLong();
    private final InputStream inputStream;
    private final ReadEvent readEvent;

    public CountingInputStream(InputStream inputStream, ReadEvent readEvent) {
        this.inputStream = inputStream;
        this.readEvent = readEvent;
    }

    public CountingInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        this.readEvent = new ReadEvent() {
            @Override
            public void onRead(long readByteCount, long totalReadByteCount) {
                // No operations.
            }
        };
    }

    @Override
    public int read() throws IOException {
        if (lock.isHeldByCurrentThread()) {
            return inputStream.read();
        } else {
            lock.lock();
            try {
                int byteValue = inputStream.read();
                if (byteValue != -1) {
                    readEvent.onRead(1L, totalReadByteCount.incrementAndGet());
                }
                return byteValue;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            return inputStream.read(bytes);
        } else {
            lock.lock();
            try {
                int readByteCount = inputStream.read(bytes);
                if (readByteCount > 0) {
                    readEvent.onRead(readByteCount, totalReadByteCount.addAndGet(readByteCount));
                }
                return readByteCount;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int read(byte[] bytes, int offset, int count) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            return inputStream.read(bytes, offset, count);
        } else {
            lock.lock();
            try {
                int readByteCount = inputStream.read(bytes, offset, count);
                if (readByteCount > 0) {
                    readEvent.onRead(readByteCount, totalReadByteCount.addAndGet(readByteCount));
                }
                return readByteCount;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public long skip(long count) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            return inputStream.skip(count);
        } else {
            lock.lock();
            try {
                long skippedByteCount = inputStream.skip(count);
                if (skippedByteCount > 0) {
                    readEvent.onRead(skippedByteCount, totalReadByteCount.addAndGet(count));
                }
                return skippedByteCount;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int available() throws IOException {
        lock.lock();
        try {
            return inputStream.available();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            inputStream.close();
        } finally {
            lock.unlock();
        }
    }

    public long getTotalReadByteCount() {
        return totalReadByteCount.get();
    }

    public interface ReadEvent {
        void onRead(long readByteCount, long totalReadByteCount);
    }
}
