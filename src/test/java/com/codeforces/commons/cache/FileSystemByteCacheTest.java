package com.codeforces.commons.cache;

import java.io.File;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 29.12.12
 */
@SuppressWarnings({"JUnitTestMethodWithNoAssertions", "ThrowableResultOfMethodCallIgnored", "ErrorNotRethrown"})
public class FileSystemByteCacheTest extends BaseByteCacheTest {
    @Override
    protected ByteCache newByteCache(File tempDir) {
        return new FileSystemByteCache(tempDir, true);
    }
}
