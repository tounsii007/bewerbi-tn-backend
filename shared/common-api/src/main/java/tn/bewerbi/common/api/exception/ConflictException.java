package tn.bewerbi.common.api.exception;

public class ConflictException extends DomainException {
    public ConflictException(String message, String messageKey) { super(message, messageKey); }
    @Override public int httpStatus() { return 409; }
    @Override public String code() { return "CONFLICT"; }
}
