package cinema.application;
import cinema.application.bookings.commands.CheckoutCartCommand;
import cinema.application.bookings.commands.CheckoutCartCommandHandler;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
class ScaffoldContractTest {
    @Test void checkoutMustNotReportFakeSuccess() {
        assertThrows(FeatureNotImplementedException.class,
            () -> new CheckoutCartCommandHandler().handle(new CheckoutCartCommand()));
    }
}
