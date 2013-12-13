package com.codeforces.commons.io;

import com.codeforces.commons.process.ThreadUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 26.12.12
 */
@ThreadSafe
public class FileBackup implements Closeable {
    private static final int DEFAULT_CONCURRENCY_LEVEL = 4;

    private final AtomicBoolean closed = new AtomicBoolean();

    private final File backupDir;
    private final boolean removeBackupDirOnClose;
    private final int concurrencyLevel;

    private final ConcurrentMap<String, BackupEntry> backupEntryByOriginalPath = new ConcurrentHashMap<String, BackupEntry>();
    private final Semaphore semaphore;

    public FileBackup(
            @Nonnull File backupDir, boolean removeBackupDirOnClose, int concurrencyLevel) throws IOException {
        this.backupDir = FileUtil.ensureDirectoryExists(backupDir);
        FileUtil.cleanDirectory(backupDir);
        this.removeBackupDirOnClose = removeBackupDirOnClose;
        this.concurrencyLevel = concurrencyLevel;
        this.semaphore = new Semaphore(concurrencyLevel);
    }

    public FileBackup(@Nonnull File backupDir, boolean removeBackupDirOnClose) throws IOException {
        this(backupDir, removeBackupDirOnClose, DEFAULT_CONCURRENCY_LEVEL);
    }

    public FileBackup(int concurrencyLevel) throws IOException {
        this.backupDir = FileUtil.createTemporaryDirectory("file-backup");
        this.removeBackupDirOnClose = true;
        this.concurrencyLevel = concurrencyLevel;
        this.semaphore = new Semaphore(concurrencyLevel);
    }

    public FileBackup() throws IOException {
        this(DEFAULT_CONCURRENCY_LEVEL);
    }

    public void backup(final File file) throws IOException {
        ensureNotClosed();

        semaphore.acquireUninterruptibly();
        try {
            ensureNotClosed();

            String originalPath = file.getCanonicalPath();
            BackupEntry backupEntry = backupEntryByOriginalPath.get(originalPath);

            if (backupEntry == null) {
                backupEntryByOriginalPath.putIfAbsent(originalPath, new BackupEntry(null));
                backupEntry = backupEntryByOriginalPath.get(originalPath);
            }

            Lock backupLock = backupEntry.getLock();

            backupLock.lock();
            try {
                File backupFile = backupEntry.getFile();

                if (backupFile != null) {
                    FileUtil.deleteTotally(backupFile);
                }

                if (file.isFile()) {
                    backupFile = FileUtil.executeIoOperation(new ThreadUtil.Operation<File>() {
                        @Override
                        public File run() throws Throwable {
                            return File.createTempFile(file.getName() + '-', "", backupDir);
                        }
                    });
                    FileUtil.copyFile(file, backupFile);
                } else if (file.isDirectory()) {
                    backupFile = FileUtil.createTemporaryDirectory(file.getName(), backupDir);
                    FileUtil.copyDirectory(file, backupFile);
                } else {
                    backupFile = null;
                }

                backupEntry.setFile(backupFile);
            } finally {
                backupLock.unlock();
            }
        } finally {
            semaphore.release();
        }
    }

    public void restoreAll() throws IOException {
        ensureNotClosed();

        semaphore.acquireUninterruptibly(concurrencyLevel);
        try {
            ensureNotClosed();

            for (Map.Entry<String, BackupEntry> entry : backupEntryByOriginalPath.entrySet()) {
                File originalFile = new File(entry.getKey());
                File backupFile = entry.getValue().getFile();

                FileUtil.deleteTotally(originalFile);

                if (backupFile != null) {
                    if (backupFile.isFile()) {
                        FileUtil.copyFile(backupFile, originalFile);
                    } else if (backupFile.isDirectory()) {
                        FileUtil.copyDirectory(backupFile, originalFile);
                    }
                }
            }
        } finally {
            semaphore.release(concurrencyLevel);
        }
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            semaphore.acquireUninterruptibly(concurrencyLevel);
            try {
                if (removeBackupDirOnClose) {
                    FileUtil.deleteTotally(backupDir);
                }
            } finally {
                semaphore.release(concurrencyLevel);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private void ensureNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("File backup is closed.");
        }
    }

    private static final class BackupEntry {
        @Nonnull
        private final Lock lock = new ReentrantLock();

        @Nullable
        private File file;

        private BackupEntry(@Nullable File file) {
            this.file = file;
        }

        @Nonnull
        public Lock getLock() {
            return lock;
        }

        @Nullable
        public File getFile() {
            return file;
        }

        public void setFile(@Nullable File file) {
            this.file = file;
        }
    }
}
