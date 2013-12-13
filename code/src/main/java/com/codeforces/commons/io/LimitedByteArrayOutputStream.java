package com.codeforces.commons.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 19.09.11
 */
@SuppressWarnings({"StandardVariableNames", "SynchronizedMethod"})
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
    public synchronized void write(int b) {
        if (size() < maxSize) {
            super.write(b);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            }
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (size() + len <= maxSize) {
            super.write(b, off, len);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            } else {
                super.write(b, off, maxSize - size());
            }
        }
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        if (size() + b.length <= maxSize) {
            super.write(b);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            } else {
                super.write(b, 0, maxSize - size());
            }
        }
    }
}
