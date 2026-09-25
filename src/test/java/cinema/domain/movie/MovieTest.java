package cinema.domain.movie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.domain.common.DomainException;

class MovieTest {

    @Test
    void validMovieKeepsValues() {
        for (MovieStatus status : MovieStatus.values()) {
            var movie = new Movie("m1", " Movie ", " Description ", 120, " T16 ", status);
            assertEquals("m1", movie.getId());
            assertEquals(" Movie ", movie.getTitle());
            assertEquals(" Description ", movie.getDescription());
            assertEquals(120, movie.getDurationMinutes());
            assertEquals(" T16 ", movie.getAgeRating());
            assertEquals(status, movie.getStatus());
        }
    }

    @Test
    void rejectsNullTitle() {
        assertThrows(DomainException.class,
                () -> new Movie("m1", null, null, 120, null, MovieStatus.COMING_SOON));
    }

    @Test
    void rejectsEmptyTitle() {
        assertThrows(DomainException.class,
                () -> new Movie("m1", "", null, 120, null, MovieStatus.COMING_SOON));
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(DomainException.class,
                () -> new Movie("m1", " \t\n", null, 120, null, MovieStatus.COMING_SOON));
    }

    @Test
    void rejectsZeroDuration() {
        assertThrows(DomainException.class,
                () -> new Movie("m1", "Movie", null, 0, null, MovieStatus.COMING_SOON));
    }

    @Test
    void rejectsNegativeDuration() {
        assertThrows(DomainException.class,
                () -> new Movie("m1", "Movie", null, -1, null, MovieStatus.COMING_SOON));
    }

    @Test
    void preservesNullMetadataAndStatusWithoutAddingRules() {
        var movie = new Movie("m1", "Movie", null, 1, null, null);
        assertNull(movie.getDescription());
        assertNull(movie.getAgeRating());
        assertNull(movie.getStatus());
        assertEquals(1, movie.getDurationMinutes());
    }
}
