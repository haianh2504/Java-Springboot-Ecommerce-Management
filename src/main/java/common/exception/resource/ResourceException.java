package common.exception.resource;

public abstract class ResourceException extends RuntimeException {
    public ResourceException(String message) {
        super(message);
    }
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
