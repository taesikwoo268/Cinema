package cinema.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies profile selection in the real application, without the Web test probe. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("venue-dev")
class VenueDevelopmentContextTest {
    @Autowired ApplicationContext context;
    @Autowired MockMvc mvc;

    @Test
    void selectsOnlyDevelopmentChainAndKeepsOtherFeaturesDenied() throws Exception {
        assertTrue(context.containsBean("venueDevSecurityFilterChain"));
        assertFalse(context.containsBean("securityFilterChain"));
        assertEquals(1, context.getBeansOfType(SecurityFilterChain.class).size());
        mvc.perform(get("/movies")).andExpect(status().isForbidden());
    }
}
