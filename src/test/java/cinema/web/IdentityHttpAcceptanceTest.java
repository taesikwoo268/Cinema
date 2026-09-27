package cinema.web;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("venue-dev")
class IdentityHttpAcceptanceTest {

    @Autowired
    TestRestTemplate http;

    private Map<String, Object> registerInput(
            String email,
            String password,
            String displayName) {

        return Map.of(
                "email", email,
                "password", password,
                "displayName", displayName);
    }

    private Map<String, Object> loginInput(
            String email,
            String password) {

        return Map.of(
                "email", email,
                "password", password);
    }

    private JsonNode body(
            ResponseEntity<JsonNode> response,
            HttpStatus expected) {

        assertEquals(
                expected,
                response.getStatusCode(),
                () -> "Response: " + response.getBody());

        assertNotNull(response.getBody());

        return response.getBody();
    }

    private void error(
            ResponseEntity<JsonNode> response,
            HttpStatus expected,
            String code) {

        JsonNode json = body(response, expected);

        assertEquals(code, json.required("code").asText());
        assertTrue(json.required("message").isTextual());
        assertFalse(json.required("message").asText().isBlank());
    }

    @Test
    void registersAndLogsInThroughRealHttp() {

        String email = "identity-" + UUID.randomUUID() + "@example.com";
        String password = "123456";

        JsonNode registered = body(
                http.postForEntity(
                        "/identity/register",
                        registerInput(email, password, "Nghia"),
                        JsonNode.class),
                HttpStatus.CREATED);

        assertTrue(registered.required("id").isTextual());
        assertFalse(registered.required("id").asText().isBlank());
        assertEquals(1, registered.size());

        JsonNode loggedIn = body(
                http.postForEntity(
                        "/identity/login",
                        loginInput(email, password),
                        JsonNode.class),
                HttpStatus.OK);

        assertTrue(loggedIn.required("accessToken").isTextual());
        assertFalse(loggedIn.required("accessToken").asText().isBlank());

        assertTrue(loggedIn.required("expiresAt").isTextual());
        assertFalse(loggedIn.required("expiresAt").asText().isBlank());
    }

    @Test
    void duplicateEmailReturnsConflict() {

        String email = "duplicate-" + UUID.randomUUID() + "@example.com";

        Map<String, Object> request =
                registerInput(email, "123456", "Duplicate User");

        body(
                http.postForEntity(
                        "/identity/register",
                        request,
                        JsonNode.class),
                HttpStatus.CREATED);

        ResponseEntity<JsonNode> duplicate =
                http.postForEntity(
                        "/identity/register",
                        request,
                        JsonNode.class);

        error(
                duplicate,
                HttpStatus.CONFLICT,
                "CONFLICT");

        assertTrue(
                duplicate.getBody()
                        .required("message")
                        .asText()
                        .contains(email));
    }

    @Test
    void invalidCredentialsReturnForbidden() {

        String email = "credentials-" + UUID.randomUUID() + "@example.com";

        body(
                http.postForEntity(
                        "/identity/register",
                        registerInput(email, "123456", "Credential User"),
                        JsonNode.class),
                HttpStatus.CREATED);

        ResponseEntity<JsonNode> login =
                http.postForEntity(
                        "/identity/login",
                        loginInput(email, "wrong-password"),
                        JsonNode.class);

        error(
                login,
                HttpStatus.FORBIDDEN,
                "FORBIDDEN");

        assertEquals(
                "Invalid credentials",
                login.getBody()
                        .required("message")
                        .asText());
    }

    @Test
    void currentUserIsUnauthenticatedWithoutAuthentication() {

        JsonNode currentUser = body(
                http.getForEntity(
                        "/identity/me",
                        JsonNode.class),
                HttpStatus.OK);

        assertTrue(currentUser.required("userId").isNull());
        assertFalse(
                currentUser.required("authenticated").asBoolean());
    }
}