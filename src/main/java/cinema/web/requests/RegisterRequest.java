package cinema.web.requests;

public record RegisterRequest(
        String email,
        String password,
        String displayName
) {
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email
                + ", password=redacted"
                + ", displayName=" + displayName + "]";
    }
}