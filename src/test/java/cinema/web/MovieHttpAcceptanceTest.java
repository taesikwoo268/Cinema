package cinema.web;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("venue-dev")
class MovieHttpAcceptanceTest {
    @Autowired TestRestTemplate http;

    private Map<String, Object> input(String title, int duration) {
        return Map.of("title", title, "description", "Space exploration",
                "durationMinutes", duration, "ageRating", "PG-13");
    }

    private JsonNode body(ResponseEntity<JsonNode> response, HttpStatus expected) {
        assertEquals(expected, response.getStatusCode(), () -> "Response: " + response.getBody());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private void error(ResponseEntity<JsonNode> response, HttpStatus expected, String code) {
        JsonNode json = body(response, expected);
        assertEquals(code, json.required("code").asText());
        assertTrue(json.required("message").isTextual());
        assertFalse(json.required("message").asText().isBlank());
    }

    @Test
    void completeMovieLifecycleThroughRealHttp() {
        JsonNode created = body(http.postForEntity("/movies", input("Interstellar", 169), JsonNode.class),
                HttpStatus.CREATED);
        assertTrue(created.required("id").isTextual());
        String id = created.required("id").asText();
        assertFalse(id.isBlank());
        assertEquals(1, created.size());

        JsonNode movie = body(http.getForEntity("/movies/" + id, JsonNode.class), HttpStatus.OK);
        assertEquals(id, movie.required("id").asText());
        assertEquals("Interstellar", movie.required("title").asText());
        assertEquals("Space exploration", movie.required("description").asText());
        assertEquals(169, movie.required("durationMinutes").asInt());
        assertEquals("PG-13", movie.required("ageRating").asText());
        assertEquals("COMING_SOON", movie.required("status").asText());

        JsonNode movies = body(http.getForEntity("/movies", JsonNode.class), HttpStatus.OK);
        assertTrue(movies.isArray());
        boolean found = false;
        for (JsonNode item : movies) {
            if (id.equals(item.path("id").asText())) {
                assertEquals(movie, item);
                found = true;
            }
        }
        assertTrue(found, "Created movie must appear in collection");

        var deleted = http.exchange("/movies/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode(), () -> "Response: " + deleted.getBody());
        assertTrue(deleted.getBody() == null || deleted.getBody().isEmpty());
        error(http.getForEntity("/movies/" + id, JsonNode.class), HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    @Test
    void blankTitleReturnsValidation() {
        error(http.postForEntity("/movies", input("   ", 120), JsonNode.class),
                HttpStatus.BAD_REQUEST, "VALIDATION");
    }

    @Test
    void zeroDurationReturnsValidation() {
        error(http.postForEntity("/movies", input("Invalid duration", 0), JsonNode.class),
                HttpStatus.BAD_REQUEST, "VALIDATION");
    }

    @Test
    void negativeDurationReturnsValidation() {
        error(http.postForEntity("/movies", input("Invalid duration", -1), JsonNode.class),
                HttpStatus.BAD_REQUEST, "VALIDATION");
    }

    @Test
    void getMissingMovieReturnsNotFound() {
        error(http.getForEntity("/movies/movie-that-does-not-exist", JsonNode.class),
                HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    @Test
    void deleteMissingMovieReturnsNotFound() {
        error(http.exchange("/movies/movie-that-does-not-exist", HttpMethod.DELETE,
                HttpEntity.EMPTY, JsonNode.class), HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    @Test
    void invalidJsonFieldTypeReturnsSanitizedValidation() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>("{\"title\":\"Movie\",\"durationMinutes\":\"not-a-number\"}", headers);
        var response = http.postForEntity("/movies", request, JsonNode.class);
        error(response, HttpStatus.BAD_REQUEST, "VALIDATION");
        assertEquals("Request body must be valid JSON with the expected field types",
                response.getBody().required("message").asText());
    }
}
