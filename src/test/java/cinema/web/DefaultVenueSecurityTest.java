package cinema.web;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The normal application profile must not expose the development venue routes. */
@SpringBootTest
@AutoConfigureMockMvc
class DefaultVenueSecurityTest {
    @Autowired MockMvc mvc;

    @ParameterizedTest
    @CsvSource({
        "GET, /cinemas", "GET, /cinemas/c1", "GET, /cinemas/c1/halls",
        "GET, /halls/h1", "GET, /halls/h1/seats",
        "POST, /cinemas", "POST, /halls", "POST, /seats"
    })
    void deniesVenueRoutesOutsideDevelopmentProfile(String method, String path) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path).with(csrf()))
            .andExpect(status().isForbidden());
    }
}

