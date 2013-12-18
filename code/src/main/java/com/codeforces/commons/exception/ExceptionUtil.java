package com.codeforces.commons.exception;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 *         Date: 17.12.12
 */
public class ExceptionUtil {
    private ExceptionUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getMessage(@Nonnull Throwable throwable) {
        String message = throwable.getMessage();

        while (message == null) {
            if ((throwable = throwable.getCause()) == null) {
                break;
            }

            message = throwable.getMessage();
        }

        return message;
    }
    
    public static String toString(Throwable e) {
        Set<Throwable> used = new HashSet<>();
        StringBuilder sb = new StringBuilder();

        while (e != null && !used.contains(e)) {
            if (!used.isEmpty()) {
                sb.append('\n');
            }

            used.add(e);
            sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append('\n');
            StackTraceElement[] elements = e.getStackTrace();
            for (StackTraceElement element : elements) {
                sb.append("    ").append(element.toString()).append('\n');
            }
            e = e.getCause();
        }

        return sb.toString();
    }
}
