package cinema.infrastructure.config;

import java.time.Instant; import cinema.application.common.interfaces.Clock; public final class SystemClock implements Clock { @Override public Instant now() { return Instant.now(); } }
