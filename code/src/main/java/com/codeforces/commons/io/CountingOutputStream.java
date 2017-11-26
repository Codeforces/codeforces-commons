package com.codeforces.commons.io;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 07.04.14
 */
@SuppressWarnings({"RefusedBequest", "MethodDoesntCallSuperMethod"})
public class CountingOutputStream extends OutputStream {
    private static final WriteEvent EMPTY_WRITE_EVENT = (writtenByteCount, totalWrittenByteCount) -> {
        // No operations.
    };

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong totalWrittenByteCount = new AtomicLong();
    private final OutputStream outputStream;
    private final WriteEvent writeEvent;

    @SuppressWarnings("WeakerAccess")
    public CountingOutputStream(@Nonnull OutputStream outputStream, @Nonnull WriteEvent writeEvent) {
        this.outputStream = outputStream;
        this.writeEvent = writeEvent;
    }

    public CountingOutputStream(@Nonnull OutputStream outputStream) {
        this(outputStream, EMPTY_WRITE_EVENT);
    }

    @Override
    public void write(int value) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            outputStream.write(value);
        } else {
            lock.lock();
            try {
                outputStream.write(value);
                writeEvent.onWrite(1L, totalWrittenByteCount.incrementAndGet());
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            outputStream.write(bytes);
        } else {
            lock.lock();
            try {
                outputStream.write(bytes);
                int count = bytes.length;
                writeEvent.onWrite(count, totalWrittenByteCount.addAndGet(count));
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            outputStream.write(bytes, offset, length);
        } else {
            lock.lock();
            try {
                outputStream.write(bytes, offset, length);
                writeEvent.onWrite(length, totalWrittenByteCount.addAndGet(length));
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        lock.lock();
        try {
            outputStream.flush();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            outputStream.close();
        } finally {
            lock.unlock();
        }
    }

    public long getTotalWrittenByteCount() {
        return totalWrittenByteCount.get();
    }

    public interface WriteEvent {
        void onWrite(long writtenByteCount, long totalWrittenByteCount) throws IOException;
    }
}
