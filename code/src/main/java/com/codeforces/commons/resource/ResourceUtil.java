package com.codeforces.commons.resource;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Edvard Davtyan
 */
public class ResourceUtil {
    private static final Logger logger = Logger.getLogger(ResourceUtil.class);

    private static final ConcurrentMap<File, ReadWriteLock> cacheLockByDirectory = new ConcurrentHashMap<>();

    @Nonnull
    public static byte[] getResource(@Nonnull Class clazz, @Nonnull String resourceName) {
        InputStream resourceInputStream = clazz.getResourceAsStream(resourceName);
        if (resourceInputStream == null) {
            throw new CantReadResourceException("Can't find resource '" + resourceName + "' for " + clazz + '.');
        }

        try {
            return IoUtil.toByteArray(resourceInputStream);
        } catch (IOException e) {
            throw new CantReadResourceException("Can't read resource '" + resourceName + "' for " + clazz + '.', e);
        }
    }

    @Nullable
    public static byte[] getResourceOrNull(@Nonnull Class clazz, @Nonnull String resourceName) {
        try {
            return getResource(clazz, resourceName);
        } catch (CantReadResourceException ignored) {
            return null;
        }
    }

    @Nonnull
    public static String getResourceAsString(@Nonnull Class clazz, @Nonnull String resourceName) {
        try {
            return new String(getResource(clazz, resourceName), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CantReadResourceException("UTF-8 is unsupported.", e);
        }
    }

    @Nullable
    public static String getResourceAsStringOrNull(@Nonnull Class clazz, @Nonnull String resourceName) {
        try {
            return getResourceAsString(clazz, resourceName);
        } catch (CantReadResourceException ignored) {
            return null;
        }
    }

    /**
     * Copies resource to the target directory.
     *
     * @param targetDirectory directory to copy file to, should exist
     * @param resource        full name of the resource
     * @throws java.io.IOException if can't perform any of I/O-operations
     */
    public static void copyResourceToDir(@Nonnull File targetDirectory, @Nonnull String resource) throws IOException {
        copyResourceToDir(targetDirectory, null, resource);
    }

    /**
     * Copies resource to the target directory or creates a symbolic link to the corresponding cache entry.
     *
     * @param targetDirectory directory to copy file to, should exist
     * @param cacheDirectory  cache directory or {@code null}
     * @param resource        full name of the resource
     * @throws java.io.IOException if can't perform any of I/O-operations
     */
    public static void copyResourceToDir(
            @Nonnull File targetDirectory, @Nullable File cacheDirectory, @Nonnull String resource)
            throws IOException {
        copyResourceToDir(targetDirectory, cacheDirectory, resource, null);
    }

    /**
     * Copies resource to the target directory or creates a symbolic link to the corresponding cache entry.
     *
     * @param targetDirectory       directory to copy file to, should exist
     * @param cacheDirectory        cache directory or {@code null}
     * @param resource              full name of the resource
     * @param overrideResourceBytes byte array to use instead of content of the resource
     * @throws java.io.IOException if can't perform any of I/O-operations
     */
    public static void copyResourceToDir(
            @Nonnull File targetDirectory, @Nullable File cacheDirectory,
            @Nonnull String resource, @Nullable byte[] overrideResourceBytes) throws IOException {
        copyResourceToDir(targetDirectory, cacheDirectory, resource, overrideResourceBytes, null);
    }

    /**
     * Copies resource to the target directory or creates a symbolic link to the corresponding cache entry.
     *
     * @param targetDirectory       directory to copy file to, should exist
     * @param cacheDirectory        cache directory or {@code null}
     * @param resource              full name of the resource
     * @param overrideResourceBytes byte array to use instead of content of the resource
     * @param resourceLoaderClass   class that will be used to load resource
     *                              or {@code null} to use {@code {@link ResourceUtil}};
     *                              ignored if {@code overrideResourceBytes} is not {@code null}
     * @throws java.io.IOException if can't perform any of I/O-operations
     */
    public static void copyResourceToDir(
            @Nonnull File targetDirectory, @Nullable File cacheDirectory, @Nonnull String resource,
            @Nullable byte[] overrideResourceBytes, @Nullable Class resourceLoaderClass) throws IOException {
        File targetFile = new File(targetDirectory, new File(resource).getName());

        if (cacheDirectory == null) {
            saveResourceToFile(targetFile, resource, overrideResourceBytes, resourceLoaderClass);
        } else {
            File cacheFile = new File(cacheDirectory, toRelativePath(resource));

            ReadWriteLock cacheLock = cacheLockByDirectory.get(cacheDirectory);
            if (cacheLock == null) {
                cacheLockByDirectory.putIfAbsent(cacheDirectory, new ReentrantReadWriteLock());
                cacheLock = cacheLockByDirectory.get(cacheDirectory);
            }

            @SuppressWarnings("TooBroadScope") boolean valid;

            Lock readLock = cacheLock.readLock();
            readLock.lock();
            try {
                valid = isCacheEntryValid(cacheFile, resource, overrideResourceBytes, resourceLoaderClass);
            } finally {
                readLock.unlock();
            }

            if (!valid) {
                Lock writeLock = cacheLock.writeLock();
                writeLock.lock();
                try {
                    if (!isCacheEntryValid(cacheFile, resource, overrideResourceBytes, resourceLoaderClass)) {
                        writeCacheEntry(cacheFile, resource, overrideResourceBytes, resourceLoaderClass);
                    }
                } finally {
                    writeLock.unlock();
                }
            }

            try {
                Files.createSymbolicLink(
                        FileSystems.getDefault().getPath(targetFile.getAbsolutePath()),
                        FileSystems.getDefault().getPath(cacheFile.getAbsolutePath())
                );
            } catch (IOException e) {
                throw new IOException(String.format(
                        "Can't create symbolic link to resource '%s' in the directory '%s'.", resource, targetDirectory
                ), e);
            }
        }
    }

    private static boolean isCacheEntryValid(
            @Nonnull File cacheFile, @Nonnull String resource,
            @Nullable byte[] overrideResourceBytes, @Nullable Class resourceLoaderClass) throws IOException {
        if (!cacheFile.isFile()) {
            return false;
        }

        InputStream resourceInputStream = null;
        InputStream cacheInputStream = null;

        try {
            if (overrideResourceBytes == null) {
                resourceInputStream = (resourceLoaderClass == null ? FileUtil.class : resourceLoaderClass)
                        .getResourceAsStream(resource);
            } else {
                resourceInputStream = new ByteArrayInputStream(overrideResourceBytes);
            }
            cacheInputStream = new BufferedInputStream(new FileInputStream(cacheFile));
            return IOUtils.contentEquals(resourceInputStream, cacheInputStream);
        } catch (IOException e) {
            throw new IOException(String.format(
                    "Can't compare resource '%s' and cache file '%s'.", resource, cacheFile
            ), e);
        } finally {
            IoUtil.closeQuietly(resourceInputStream, cacheInputStream);
        }
    }

    private static void writeCacheEntry(
            @Nonnull File cacheFile, @Nonnull String resource,
            @Nullable byte[] overrideResourceBytes, @Nullable Class resourceLoaderClass) throws IOException {
        logger.info(String.format("Saving resource '%s' to the cache file '%s'.", resource, cacheFile));

        try {
            FileUtil.deleteTotally(cacheFile);
        } catch (IOException e) {
            throw new IOException(String.format("Can't delete invalid cache file '%s'.", cacheFile), e);
        }

        try {
            FileUtil.ensureParentDirectoryExists(cacheFile);
        } catch (IOException e) {
            throw new IOException(String.format("Can't create cache directory '%s'.", cacheFile.getParentFile()), e);
        }

        saveResourceToFile(cacheFile, resource, overrideResourceBytes, resourceLoaderClass);
    }

    /**
     * Writes resource content or byte array to the specified target file.
     * {@code overrideResourceBytes} parameter has greater priority.
     *
     * @param targetFile            file to write resource to, parent directory should exist
     * @param resource              resource name or {@code null};
     *                              ignored if {@code overrideResourceBytes} is not {@code null};
     *                              shouldn't be {@code null} if {@code overrideResourceBytes} is {@code null}
     * @param overrideResourceBytes resource bytes or {@code null}
     * @param resourceLoaderClass   that will be used to load resource
     *                              or {@code null} to use {@code {@link ResourceUtil}};
     *                              ignored if {@code overrideResourceBytes} is not {@code null}
     * @throws java.io.IOException if can't perform any of I/O-operations
     */
    public static void saveResourceToFile(
            @Nonnull File targetFile, @Nullable String resource,
            @Nullable byte[] overrideResourceBytes, @Nullable Class resourceLoaderClass) throws IOException {
        InputStream resourceInputStream = null;
        OutputStream cacheOutputStream = null;

        try {
            if (overrideResourceBytes == null) {
                resourceInputStream = (resourceLoaderClass == null ? FileUtil.class : resourceLoaderClass)
                        .getResourceAsStream(resource);
                cacheOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                IoUtil.copy(resourceInputStream, cacheOutputStream);
            } else {
                FileUtil.writeFile(targetFile, overrideResourceBytes);
            }
        } catch (IOException e) {
            throw new IOException(String.format("Can't save resource '%s' to the file '%s'.", resource, targetFile), e);
        } finally {
            IoUtil.closeQuietly(resourceInputStream, cacheOutputStream);
        }
    }

    private static String toRelativePath(String resource) {
        while (resource.startsWith(File.separator)) {
            resource = resource.substring(File.separator.length());
        }

        while (SeparatorHolder.SEPARATOR != null && resource.startsWith(SeparatorHolder.SEPARATOR)) {
            resource = resource.substring(SeparatorHolder.SEPARATOR.length());
        }

        while (resource.startsWith("/")) {
            resource = resource.substring("/".length());
        }

        while (resource.startsWith("\\")) {
            resource = resource.substring("\\".length());
        }

        return resource;
    }

    private static final class SeparatorHolder {
        private static final String SEPARATOR;

        static {
            String separator;
            try {
                FileSystem fileSystem = FileSystems.getDefault();
                separator = fileSystem == null ? null : fileSystem.getSeparator();
            } catch (RuntimeException e) {
                logger.fatal("Can't get path separator.", e);
                separator = null;
            }
            SEPARATOR = separator;
        }

        private SeparatorHolder() {
            throw new UnsupportedOperationException();
        }
    }
}
