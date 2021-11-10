package com.codeforces.commons.resource;

/**
 * @author Edvard Davtyan
 */
public class CantReadResourceException extends RuntimeException {
    public CantReadResourceException(String message) {
        super(message);
    }

    public CantReadResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
