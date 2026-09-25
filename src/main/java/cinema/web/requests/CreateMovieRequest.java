package cinema.web.requests;

public record CreateMovieRequest(String title, String description, int durationMinutes, String ageRating) { }
