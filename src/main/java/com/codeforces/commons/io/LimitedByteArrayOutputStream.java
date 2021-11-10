package com.codeforces.commons.io;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 19.09.11
 */
public class LimitedByteArrayOutputStream extends ByteArrayOutputStream {
    private final int maxSize;
    private final boolean throwIfExceeded;

    public LimitedByteArrayOutputStream(@Nonnegative int maxSize, boolean throwIfExceeded) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Argument 'maxSize' (" + maxSize + " B) is negative.");
        }

        this.maxSize = maxSize;
        this.throwIfExceeded = throwIfExceeded;
    }

    @Override
    public void write(int value) {
        if (size() < maxSize) {
            super.write(value);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            }
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) {
        if (size() + length <= maxSize) {
            super.write(bytes, offset, length);
        } else {
            if (throwIfExceeded) {
                throw new IllegalStateException("Buffer size (" + maxSize + " B) exceeded.");
            } else {
                super.write(bytes, offset, maxSize - size());
            }
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes) throws IOException {
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
