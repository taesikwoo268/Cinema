package cinema.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cinema.application.common.interfaces.CurrentUser;

@Configuration
public class IdentitySecurityConfiguration {

    @Bean
    public CurrentUser currentUser() {
        return new SpringCurrentUser();
    }
}