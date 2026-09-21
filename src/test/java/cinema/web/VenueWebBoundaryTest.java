package cinema.web;

import cinema.web.exception.GlobalExceptionHandler;
import cinema.web.requests.CreateCinemaRequest;
import cinema.web.requests.CreateHallRequest;
import cinema.web.requests.CreateSeatRequest;
import cinema.web.responses.CreatedResourceResponse;
import cinema.web.security.VenueDevSecurityConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Isolated security/JSON contract tests. ProbeController is a test fixture only,
 * not an implementation of venue use cases or evidence of M1 integration.
 */
@WebMvcTest
@ContextConfiguration(classes = {
    VenueWebBoundaryTest.ProbeController.class,
    VenueDevSecurityConfig.class,
    GlobalExceptionHandler.class
})
@ActiveProfiles("venue-dev")
class VenueWebBoundaryTest {
    @Autowired MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {
        "/cinemas", "/cinemas/c1", "/cinemas/c1/halls",
        "/halls/h1", "/halls/h1/seats"
    })
    void allowsOnlySpecifiedGetRoutes(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().string("probe"));
    }

    @ParameterizedTest
    @CsvSource({
        "GET, /movies", "GET, /halls", "GET, /seats",
        "GET, /seats/s1", "GET, /cinemas/c1/halls/h1",
        "GET, /halls/h1/seats/s1", "GET, /cinemas/c1/internal",
        "POST, /cinemas/c1", "POST, /cinemas/c1/halls",
        "POST, /halls/h1/seats", "POST, /movies",
        "PUT, /cinemas", "PATCH, /halls", "DELETE, /seats",
        "DELETE, /cinemas/c1", "HEAD, /cinemas", "OPTIONS, /cinemas"
    })
    void deniesOtherRoutesAndMethodsEvenWithValidCsrf(String method, String path) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void acceptsCinemaJsonWithoutCsrfOnCreateRoute() throws Exception {
        mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cinema One\",\"address\":\"Address One\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Cinema One"))
            .andExpect(jsonPath("$.address").value("Address One"));
    }

    @Test
    void acceptsHallJsonWithoutCsrfOnCreateRoute() throws Exception {
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cinemaId\":\"c1\",\"name\":\"Hall One\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cinemaId").value("c1"));
    }

    @Test
    void preservesSeatInputForApplicationValidation() throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON)
                .content("{\"hallId\":\"h1\",\"row\":\" a \",\"number\":1,\"type\":\"NORMAL\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.row").value(" a "))
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.type").value("NORMAL"));
    }

    @Test
    void missingSeatNumberRemainsNullInsteadOfBecomingZero() throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON)
                .content("{\"hallId\":\"h1\",\"row\":\"A\",\"type\":\"NORMAL\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"number\":\"not-a-number\"}", "[]"})
    void malformedJsonReturnsSanitizedValidationError(String json) throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION"))
            .andExpect(jsonPath("$.message").value(
                "Request body must be valid JSON with the expected field types"));
    }

    @Test
    void missingBodyReturnsValidationError() throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void createdResourceResponseContainsOnlyId() throws Exception {
        mvc.perform(get("/cinemas/response-shape"))
            .andExpect(status().isCreated())
            .andExpect(content().string("{\"id\":\"probe-id\"}"));
    }

    @RestController
    static class ProbeController {
        @PostMapping("/cinemas")
        CreateCinemaRequest cinema(@RequestBody CreateCinemaRequest input) { return input; }

        @PostMapping("/halls")
        CreateHallRequest hall(@RequestBody CreateHallRequest input) { return input; }

        @PostMapping("/seats")
        CreateSeatRequest seat(@RequestBody CreateSeatRequest input) { return input; }

        @GetMapping("/cinemas/response-shape")
        @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
        CreatedResourceResponse responseShape() { return new CreatedResourceResponse("probe-id"); }

        // A fallback makes unintended access observable as 200 rather than an unrelated 404.
        @RequestMapping("/**")
        String probe() { return "probe"; }
    }
}
