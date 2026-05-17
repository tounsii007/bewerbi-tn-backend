package tn.bewerbi.common.api.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message, String messageKey) { super(message, messageKey); }
    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(
                resource + " not found: " + id,
                "error." + resource.toLowerCase() + ".notFound");
    }
    @Override public int httpStatus() { return 404; }
    @Override public String code() { return "NOT_FOUND"; }
}
