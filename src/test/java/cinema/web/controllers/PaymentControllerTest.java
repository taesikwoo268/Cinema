package cinema.web.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cinema.application.common.models.PaymentResult;
import cinema.application.payments.commands.PayBookingCommand;
import cinema.application.payments.commands.PayBookingCommandHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private final PayBookingCommandHandler payBooking =
            mock(PayBookingCommandHandler.class);

    private final PaymentController controller =
            new PaymentController(payBooking);

    private MockMvc http;

    @BeforeEach
    void setup() {
        http = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void payMapsBookingIdAndReturnsSuccessfulResult() throws Exception {
        PaymentResult result =
                new PaymentResult(true, "FAKE-PAY-123");

        when(payBooking.handle(
                new PayBookingCommand("booking-1")))
                .thenReturn(result);

        http.perform(post("/payments/booking-1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "successful": true,
                          "providerReference": "FAKE-PAY-123"
                        }
                        """, true));

        verify(payBooking).handle(
                new PayBookingCommand("booking-1"));
        verifyNoMoreInteractions(payBooking);
    }

    @Test
    void unsuccessfulPaymentStillReturnsOkWithResult() throws Exception {
        PaymentResult result =
                new PaymentResult(false, "FAKE-PAY-FAILED");

        when(payBooking.handle(
                new PayBookingCommand("booking-2")))
                .thenReturn(result);

        http.perform(post("/payments/booking-2"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "successful": false,
                          "providerReference": "FAKE-PAY-FAILED"
                        }
                        """, true));

        verify(payBooking).handle(
                new PayBookingCommand("booking-2"));
        verifyNoMoreInteractions(payBooking);
    }

    @Test
    void directCallReturnsHandlerResult() {
        PaymentResult result =
                new PaymentResult(true, "FAKE-PAY-999");

        when(payBooking.handle(
                new PayBookingCommand("booking-9")))
                .thenReturn(result);

        PaymentResult actual =
                controller.pay("booking-9");

        org.junit.jupiter.api.Assertions.assertSame(result, actual);

        verify(payBooking).handle(
                new PayBookingCommand("booking-9"));
        verifyNoMoreInteractions(payBooking);
    }
}