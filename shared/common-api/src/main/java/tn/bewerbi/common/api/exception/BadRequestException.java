package tn.bewerbi.common.api.exception;

public class BadRequestException extends DomainException {
    public BadRequestException(String message, String messageKey) { super(message, messageKey); }
    @Override public int httpStatus() { return 400; }
    @Override public String code() { return "BAD_REQUEST"; }
}
