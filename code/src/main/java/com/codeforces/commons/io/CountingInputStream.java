package com.codeforces.commons.io;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.04.14
 */
@SuppressWarnings({"RefusedBequest", "MethodDoesntCallSuperMethod"})
public final class CountingInputStream extends InputStream {
    private static final ReadEvent EMPTY_READ_EVENT = (readByteCount, totalReadByteCount) -> {
        // No operations.
    };

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong totalReadByteCount = new AtomicLong();
    private final InputStream inputStream;
    private final ReadEvent readEvent;

    public CountingInputStream(@Nonnull InputStream inputStream, @Nonnull ReadEvent readEvent) {
        this.inputStream = inputStream;
        this.readEvent = readEvent;
    }

    public CountingInputStream(@Nonnull InputStream inputStream) {
        this(inputStream, EMPTY_READ_EVENT);
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
    public int read(@Nonnull byte[] bytes) throws IOException {
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
    public int read(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            return inputStream.read(bytes, offset, length);
        } else {
            lock.lock();
            try {
                int readByteCount = inputStream.read(bytes, offset, length);
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

    @Contract(pure = true)
    public long getTotalReadByteCount() {
        return totalReadByteCount.get();
    }

    public interface ReadEvent {
        void onRead(long readByteCount, long totalReadByteCount) throws IOException;
    }
}
