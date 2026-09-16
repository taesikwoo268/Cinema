package cinema.application.common.exceptions;

public final class FeatureNotImplementedException extends RuntimeException {
    public FeatureNotImplementedException(String feature) { super(feature + " is not implemented in Architecture Contract v0.1"); }
}
