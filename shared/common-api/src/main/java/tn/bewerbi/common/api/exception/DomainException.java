package tn.bewerbi.common.api.exception;

public abstract class DomainException extends RuntimeException {
    private final String messageKey;
    protected DomainException(String message, String messageKey) {
        super(message);
        this.messageKey = messageKey;
    }
    public String messageKey() { return messageKey; }
    public abstract int httpStatus();
    public abstract String code();
}
