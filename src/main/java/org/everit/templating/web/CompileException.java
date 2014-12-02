package org.everit.templating.web;

public class CompileException extends RuntimeException {

    public CompileException(final String message) {
        super(message);
    }

    public CompileException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CompileException(final Throwable cause) {
        super(cause);
    }

}
