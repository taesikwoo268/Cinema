package cinema.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("venue-dev")
class ScreeningHttpAcceptanceTest {

    private static final Instant TEN = Instant.parse("2030-10-01T10:00:00Z");
    private static final Instant TWELVE = Instant.parse("2030-10-01T12:00:00Z");

    @Autowired
    TestRestTemplate http;

    private String create(String path, Map<String, Object> input) {
        ResponseEntity<JsonNode> response = http.postForEntity(path, input, JsonNode.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), () -> "Response: " + response.getBody());
        return response.getBody().required("id").asText();
    }

    private String[] prerequisites() {
        String suffix = UUID.randomUUID().toString();
        String movie = create("/movies", Map.of(
                "title", "Screening Movie " + suffix,
                "description", "Acceptance test movie",
                "durationMinutes", 120,
                "ageRating", "PG-13"));
        String cinema = create("/cinemas", Map.of("name", "Screening Cinema " + suffix, "address", "Address"));
        String hall = create("/halls", Map.of("cinemaId", cinema, "name", "Hall"));
        return new String[]{movie, hall};
    }

    private Map<String, Object> screening(String movie, String hall, Instant start, Instant end, Object price) {
        return Map.of("movieId", movie, "hallId", hall, "startTime", start.toString(),
                "endTime", end.toString(), "basePrice", price, "currency", "VND");
    }

    @Test
    void createsReadsListsAndCancelsScreeningWithoutDeletingIt() {
        String[] parents = prerequisites();
        String movie = parents[0];
        String hall = parents[1];
        String screening = create("/screenings", screening(movie, hall, TEN, TWELVE, "100000.00"));

        JsonNode created = http.getForEntity("/screenings/" + screening, JsonNode.class).getBody();
        assertEquals(screening, created.required("id").asText());
        assertEquals(movie, created.required("movieId").asText());
        assertEquals(hall, created.required("hallId").asText());
        assertEquals(TEN.toString(), created.required("startTime").asText());
        assertEquals(TWELVE.toString(), created.required("endTime").asText());
        assertEquals(0, new BigDecimal("100000.00").compareTo(created.required("basePrice").decimalValue()));
        assertEquals("VND", created.required("currency").asText());
        assertEquals("SCHEDULED", created.required("status").asText());

        JsonNode list = http.getForEntity("/screenings?movieId=" + movie, JsonNode.class).getBody();
        assertTrue(list.isArray());
        assertTrue(list.toString().contains(screening));

        ResponseEntity<String> cancelled = http.postForEntity("/screenings/" + screening + "/cancel", null, String.class);
        assertEquals(HttpStatus.NO_CONTENT, cancelled.getStatusCode());
        assertTrue(cancelled.getBody() == null || cancelled.getBody().isEmpty());
        assertEquals("CANCELLED", http.getForEntity("/screenings/" + screening, JsonNode.class)
                .getBody().required("status").asText());

        ResponseEntity<JsonNode> cancelledAgain = http.postForEntity("/screenings/" + screening + "/cancel", null, JsonNode.class);
        assertEquals(HttpStatus.CONFLICT, cancelledAgain.getStatusCode());
        assertEquals("CONFLICT", cancelledAgain.getBody().required("code").asText());
        assertTrue(!cancelledAgain.getBody().required("message").asText().isBlank());
        assertEquals("CANCELLED", http.getForEntity("/screenings/" + screening, JsonNode.class)
                .getBody().required("status").asText());
    }

    @Test
    void rejectsInvalidCreateAndMissingResources() {
        String[] parents = prerequisites();
        String movie = parents[0];
        String hall = parents[1];

        assertEquals(HttpStatus.BAD_REQUEST, http.postForEntity("/screenings",
                screening(" ", hall, TEN, TWELVE, "100000"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, http.postForEntity("/screenings",
                screening(movie, " ", TEN, TWELVE, "100000"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, http.postForEntity("/screenings",
                screening(movie, hall, TWELVE, TEN, "100000"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, http.postForEntity("/screenings",
                screening(movie, hall, TEN, TWELVE, "-1"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.postForEntity("/screenings",
                screening("missing-movie", hall, TEN, TWELVE, "100000"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.postForEntity("/screenings",
                screening(movie, "missing-hall", TEN, TWELVE, "100000"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.getForEntity("/screenings/missing", JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.postForEntity("/screenings/missing/cancel", null, String.class).getStatusCode());
    }

    @Test
    void rejectsOverlapButAllowsTouchingBoundary() {
        String[] parents = prerequisites();
        String movie = parents[0];
        String hall = parents[1];
        create("/screenings", screening(movie, hall, TEN, TWELVE, "100000"));

        assertEquals(HttpStatus.CONFLICT, http.postForEntity("/screenings",
                screening(movie, hall, Instant.parse("2030-10-01T11:00:00Z"),
                        Instant.parse("2030-10-01T13:00:00Z"), "100000"), JsonNode.class).getStatusCode());
        assertEquals(HttpStatus.CREATED, http.postForEntity("/screenings",
                screening(movie, hall, TWELVE, Instant.parse("2030-10-01T13:00:00Z"), "100000"), JsonNode.class).getStatusCode());
    }

    @Test
    void malformedJsonReturnsSanitizedValidationAndUnsupportedRoutesAreDenied() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"basePrice\":\"not-a-number\"}", headers);
        ResponseEntity<JsonNode> malformed = http.postForEntity("/screenings", request, JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, malformed.getStatusCode());
        assertEquals("VALIDATION", malformed.getBody().required("code").asText());
        assertEquals("Request body must be valid JSON with the expected field types",
                malformed.getBody().required("message").asText());

        assertEquals(HttpStatus.FORBIDDEN, http.getForEntity("/screenings/unknown/extra", String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, http.postForEntity("/screenings/unknown", null, String.class).getStatusCode());
    }
}
