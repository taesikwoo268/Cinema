package cinema.infrastructure.identity;

public record ApplicationUser(String id, String email, String passwordHash) {

    @Override
    public String toString() {
        return "ApplicationUser[id=" + id + "]";
    }
}
