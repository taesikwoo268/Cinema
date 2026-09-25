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
class VenueHttpAcceptanceTest {
    @Autowired TestRestTemplate http;

    private String create(String path, Map<String, Object> input) {
        var response = http.postForEntity(path, input, JsonNode.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody().required("id").asText();
    }

    @Test
    void realHttpCreatesReadsAndRejectsInvalidMissingAndDuplicateResources() {
        String cinema = create("/cinemas", Map.of("name", " HTTP Cinema ", "address", " Address "));
        String hall = create("/halls", Map.of("cinemaId", cinema, "name", "Hall"));
        String seat = create("/seats", Map.of("hallId", hall, "row", " a ", "number", 1, "type", "NORMAL"));
        var cinemaResponse = http.getForEntity("/cinemas/" + cinema, JsonNode.class);
        assertEquals(HttpStatus.OK, cinemaResponse.getStatusCode());
        assertEquals("HTTP Cinema", cinemaResponse.getBody().required("name").asText());
        var halls = http.getForEntity("/cinemas/" + cinema + "/halls", JsonNode.class);
        assertEquals(HttpStatus.OK, halls.getStatusCode());
        assertEquals(hall, halls.getBody().get(0).required("id").asText());
        var seats = http.getForEntity("/halls/" + hall + "/seats", JsonNode.class);
        assertEquals(HttpStatus.OK, seats.getStatusCode());
        assertEquals(seat, seats.getBody().get(0).required("id").asText());
        assertEquals("A", seats.getBody().get(0).required("row").asText());

        var invalid = http.postForEntity("/seats", Map.of("hallId", hall, "row", "A", "number", 0, "type", "NORMAL"), JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatusCode());
        assertEquals("VALIDATION", invalid.getBody().required("code").asText());
        var missing = http.postForEntity("/halls", Map.of("cinemaId", "missing", "name", "Hall"), JsonNode.class);
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("NOT_FOUND", missing.getBody().required("code").asText());
        var duplicate = http.postForEntity("/seats", Map.of("hallId", hall, "row", "A", "number", 1, "type", "VIP"), JsonNode.class);
        assertEquals(HttpStatus.CONFLICT, duplicate.getStatusCode());
        assertEquals("CONFLICT", duplicate.getBody().required("code").asText());
        var duplicateHall = http.postForEntity("/halls", Map.of("cinemaId", cinema, "name", " Hall "), JsonNode.class);
        assertEquals(HttpStatus.CONFLICT, duplicateHall.getStatusCode());
        assertEquals("CONFLICT", duplicateHall.getBody().required("code").asText());
        assertEquals(1, http.getForObject("/halls/" + hall + "/seats", JsonNode.class).size());
        String otherHall = create("/halls", Map.of("cinemaId", cinema, "name", "Other"));
        create("/seats", Map.of("hallId", otherHall, "row", "A", "number", 1, "type", "VIP"));
        assertEquals(HttpStatus.FORBIDDEN, http.getForEntity("/unimplemented", String.class).getStatusCode());
    }
}
