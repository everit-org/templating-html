package org.everit.osgi.ewt;

public class RenderException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -5240158866618478489L;

    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }

}
