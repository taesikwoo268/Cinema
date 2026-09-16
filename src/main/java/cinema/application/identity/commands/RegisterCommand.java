package cinema.application.identity.commands;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record RegisterCommand(String email, String password) { }
