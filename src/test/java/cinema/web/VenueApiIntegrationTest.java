package cinema.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("venue-dev")
class VenueApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    private String create(String route, String body) throws Exception {
        var response = mvc.perform(post(route).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).required("id").asText();
    }

    @Test
    void readsAllRoutesAndDistinguishesEmptyChildrenFromMissingParents() throws Exception {
        String cinemaId = create("/cinemas", "{\"name\":\"Empty Cinema\",\"address\":\"Address\"}");
        mvc.perform(get("/cinemas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + cinemaId + "')].name").value(org.hamcrest.Matchers.hasItem("Empty Cinema")));
        mvc.perform(get("/cinemas/" + cinemaId + "/halls")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        String hallId = create("/halls", "{\"cinemaId\":\"" + cinemaId + "\",\"name\":\"Empty Hall\"}");
        mvc.perform(get("/halls/" + hallId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hallId)).andExpect(jsonPath("$.cinemaId").value(cinemaId))
                .andExpect(jsonPath("$.name").value("Empty Hall"));
        mvc.perform(get("/halls/" + hallId + "/seats")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        for (String path : new String[]{"/cinemas/missing", "/halls/missing", "/cinemas/missing/halls", "/halls/missing/seats"}) {
            mvc.perform(get(path)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        }
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cinemaId\":\"missing\",\"name\":\"Hall\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateSeatReturnsConflictWithoutChangingOriginal() throws Exception {
        String cinemaId = create("/cinemas", "{\"name\":\"Seat Cinema\",\"address\":\"Address\"}");
        String hallId = create("/halls", "{\"cinemaId\":\"" + cinemaId + "\",\"name\":\"Hall\"}");
        String seatId = create("/seats", "{\"hallId\":\"" + hallId + "\",\"row\":\"a\",\"number\":1,\"type\":\"COUPLE\"}");
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON)
                .content("{\"hallId\":\"" + hallId + "\",\"row\":\" A \",\"number\":1,\"type\":\"VIP\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
        mvc.perform(get("/halls/" + hallId + "/seats")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(seatId))
                .andExpect(jsonPath("$[0].hallId").value(hallId)).andExpect(jsonPath("$[0].row").value("A"))
                .andExpect(jsonPath("$[0].number").value(1)).andExpect(jsonPath("$[0].type").value("COUPLE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"hallId\":\"missing\",\"row\":\"A\",\"type\":\"NORMAL\"}",
        "{\"hallId\":\"missing\",\"row\":\"A\",\"number\":0,\"type\":\"NORMAL\"}",
        "{\"hallId\":\"missing\",\"row\":\" \",\"number\":1,\"type\":\"NORMAL\"}",
        "{\"hallId\":\"missing\",\"number\":1,\"type\":\"NORMAL\"}",
        "{\"hallId\":\"missing\",\"row\":\"A\",\"number\":1}",
        "{\"hallId\":\"missing\",\"row\":\"A\",\"number\":1,\"type\":\"normal\"}"
    })
    void invalidSeatInputReturnsValidationBeforeParentLookup(String body) throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void invalidCinemaAndHallReturnValidation() throws Exception {
        mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Cinema\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON).content("{\"cinemaId\":\"missing\",\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void createsCinemaHallAndSeatThenReadsHierarchy() throws Exception {
        String cinemaId = mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  Aurora  \",\"address\":\"  Main street  \"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        String hallId = mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cinemaId\":\"" + cinemaId + "\",\"name\":\"Hall A\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON)
                .content("{\"hallId\":\"" + hallId + "\",\"row\":\" a \",\"number\":1,\"type\":\"NORMAL\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/cinemas/" + cinemaId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aurora"))
                .andExpect(jsonPath("$.address").value("Main street"));
        mvc.perform(get("/cinemas/" + cinemaId + "/halls")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hall A"));
        mvc.perform(get("/halls/" + hallId + "/seats")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].row").value("A"));
    }

    @Test
    void validatesMissingParentsMalformedJsonAndDuplicates() throws Exception {
        mvc.perform(post("/seats").contentType(MediaType.APPLICATION_JSON).content("{\"hallId\":\"missing\",\"row\":\"A\",\"number\":1,\"type\":\"NORMAL\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        String cinemaId = mvc.perform(post("/cinemas").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Conflict Cinema\",\"address\":\"Address\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString().replaceAll(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON).content("{\"cinemaId\":\"" + cinemaId + "\",\"name\":\"Same\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/halls").contentType(MediaType.APPLICATION_JSON).content("{\"cinemaId\":\"" + cinemaId + "\",\"name\":\" Same \"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void keepsDefaultRoutesClosed() throws Exception {
        mvc.perform(get("/movies")).andExpect(status().isForbidden());
    }
}
