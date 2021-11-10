package com.codeforces.commons.io;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The clone of {@link java.io.ByteArrayOutputStream} except it is more memory efficient and not ambiguously
 * synchronized.
 *
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 23.11.2017
 */
@NotThreadSafe
public class ByteArrayOutputStream extends OutputStream {
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    private byte[] buffer;
    private int size;

    public ByteArrayOutputStream() {
        this(32);
    }

    public ByteArrayOutputStream(@Nonnegative int size) {
        Preconditions.checkArgument(size >= 0, "Negative initial size: " + size + '.');
        buffer = new byte[size];
    }

    private void ensureCapacity(@Nonnegative int capacity) {
        int oldCapacity = buffer.length;
        if (capacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) >> 1;
            if (newCapacity < capacity) {
                newCapacity = capacity;
            }
            if (newCapacity > MAX_BUFFER_SIZE) {
                newCapacity = capacity > MAX_BUFFER_SIZE ? Integer.MAX_VALUE : MAX_BUFFER_SIZE;
            }
            buffer = Arrays.copyOf(buffer, newCapacity);
        }
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Override
    public void write(int value) {
        ensureCapacity(size + 1);
        buffer[size] = (byte) value;
        size += 1;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public void write(@Nonnull byte[] bytes, @Nonnegative int offset, @Nonnegative int length) {
        if (offset < 0 || offset > bytes.length || length < 0 || offset + length - bytes.length > 0) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(size + length);
        System.arraycopy(bytes, offset, buffer, size, length);
        size += length;
    }

    public void writeTo(@Nonnull OutputStream out) throws IOException {
        out.write(buffer, 0, size);
    }

    public void reset() {
        size = 0;
    }

    @Contract(pure = true)
    @Nonnull
    public byte[] toByteArray() {
        return Arrays.copyOf(buffer, size);
    }

    @Contract(pure = true)
    public int size() {
        return size;
    }

    @Nonnull
    public String toString() {
        return new String(buffer, 0, size, StandardCharsets.UTF_8);
    }

    @Nonnull
    public String toString(@Nonnull String charsetName) throws UnsupportedEncodingException {
        return new String(buffer, 0, size, charsetName);
    }

    @Nonnull
    public String toString(@Nonnull Charset charset) {
        return new String(buffer, 0, size, charset);
    }

    @Override
    public void close() {
        // No operations.
    }
}
