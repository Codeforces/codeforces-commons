package com.codeforces.commons.io;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 19.09.11
 */
@SuppressWarnings("SynchronizedMethod")
public class LimitedByteArrayOutputStream extends ByteArrayOutputStream {
    private final int maxSize;
    private final boolean throwIfExceeded;

    public LimitedByteArrayOutputStream(int maxSize, boolean throwIfExceeded) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Argument 'maxSize' (" + maxSize + " B) is negative.");
        }

        this.maxSize = maxSize;
        this.throwIfExceeded = throwIfExceeded;
    }

    @Override
    public synchronized void write(int value) {
        if (size() < maxSize) {
            super.write(value);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            }
        }
    }

    @Override
    public synchronized void write(byte[] bytes, int off, int len) {
        if (size() + len <= maxSize) {
            super.write(bytes, off, len);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            } else {
                super.write(bytes, off, maxSize - size());
            }
        }
    }

    @Override
    public synchronized void write(@Nonnull byte[] bytes) throws IOException {
        if (size() + bytes.length <= maxSize) {
            super.write(bytes);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            } else {
                super.write(bytes, 0, maxSize - size());
            }
        }
    }
}
