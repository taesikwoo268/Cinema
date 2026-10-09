package cinema.application.common.models;

public record AuthenticatedUser(String userId, String role) {
    public AuthenticatedUser {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("User ID must not be blank");
        if (!"CUSTOMER".equals(role) && !"ADMIN".equals(role)) throw new IllegalArgumentException("Unsupported role");
    }
    public AuthenticatedUser(String userId) { this(userId, "CUSTOMER"); }
}
