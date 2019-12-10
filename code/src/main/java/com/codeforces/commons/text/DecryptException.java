package com.codeforces.commons.text;

public class DecryptException extends Exception {
    DecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    DecryptException(String message) {
        super(message);
    }
}
