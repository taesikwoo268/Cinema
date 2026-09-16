package cinema.application.movies.commands;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record DeleteMovieCommand(String movieId) { }
