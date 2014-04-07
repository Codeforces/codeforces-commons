package com.codeforces.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 07.04.14
 */
@SuppressWarnings("RefusedBequest")
public class CountingOutputStream extends OutputStream {
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong totalWrittenByteCount = new AtomicLong();
    private final OutputStream outputStream;
    private final WriteEvent writeEvent;

    public CountingOutputStream(OutputStream outputStream, WriteEvent writeEvent) {
        this.outputStream = outputStream;
        this.writeEvent = writeEvent;
    }

    public CountingOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.writeEvent = new WriteEvent() {
            @Override
            public void onWrite(long writtenByteCount, long totalWrittenByteCount) {
                // No operations.
            }
        };
    }

    @Override
    public void write(int byteValue) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            outputStream.write(byteValue);
        } else {
            lock.lock();
            try {
                outputStream.write(byteValue);
                writeEvent.onWrite(1L, totalWrittenByteCount.incrementAndGet());
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
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
    public void write(byte[] bytes, int offset, int count) throws IOException {
        if (lock.isHeldByCurrentThread()) {
            outputStream.write(bytes, offset, count);
        } else {
            lock.lock();
            try {
                outputStream.write(bytes, offset, count);
                writeEvent.onWrite(count, totalWrittenByteCount.addAndGet(count));
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
        void onWrite(long writtenByteCount, long totalWrittenByteCount);
    }
}
