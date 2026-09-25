package cinema.application.common.models;

import cinema.domain.movie.MovieStatus;

public record MovieView(String id, String title, String description, int durationMinutes,
                        String ageRating, MovieStatus status) { }
