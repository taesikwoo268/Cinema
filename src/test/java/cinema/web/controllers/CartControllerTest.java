package cinema.web.controllers;

import cinema.application.carts.commands.AddSeatToCartCommand;
import cinema.application.carts.commands.AddSeatToCartCommandHandler;
import cinema.application.carts.commands.ClearCartCommand;
import cinema.application.carts.commands.ClearCartCommandHandler;
import cinema.application.carts.commands.RemoveSeatFromCartCommand;
import cinema.application.carts.commands.RemoveSeatFromCartCommandHandler;
import cinema.application.carts.queries.GetMyCartQuery;
import cinema.application.carts.queries.GetMyCartQueryHandler;
import cinema.application.common.models.CartItemView;
import cinema.application.common.models.CartView;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {
    private final AddSeatToCartCommandHandler add = mock(AddSeatToCartCommandHandler.class);
    private final RemoveSeatFromCartCommandHandler remove = mock(RemoveSeatFromCartCommandHandler.class);
    private final ClearCartCommandHandler clear = mock(ClearCartCommandHandler.class);
    private final GetMyCartQueryHandler get = mock(GetMyCartQueryHandler.class);
    private final CartController controller = new CartController(add, remove, clear, get);
    private MockMvc http;

    @BeforeEach
    void setup() {
        http = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void postSeatMapsBodyAndReturnsNoContent() throws Exception {
        http.perform(post("/carts/seats").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"screeningId\":\"screening-1\",\"seatId\":\"seat-2\"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(add).handle(new AddSeatToCartCommand("screening-1", "seat-2"));
        verifyNoMoreInteractions(add);
        verifyNoInteractions(remove, clear, get);
    }

    @Test
    void deleteSeatMapsBodyAndReturnsNoContent() throws Exception {
        http.perform(delete("/carts/seats").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"screeningId\":\"screening-3\",\"seatId\":\"seat-4\"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(remove).handle(new RemoveSeatFromCartCommand("screening-3", "seat-4"));
        verifyNoMoreInteractions(remove);
        verifyNoInteractions(add, clear, get);
    }

    @Test
    void deleteCartDelegatesClearAndReturnsNoContent() throws Exception {
        http.perform(delete("/carts"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(clear).handle(new ClearCartCommand());
        verifyNoMoreInteractions(clear);
        verifyNoInteractions(add, remove, get);
    }

    @Test
    void getMeReturnsMappedCartView() throws Exception {
        CartView view = new CartView("cart-1", "customer-1", List.of(
                new CartItemView("s2", "a2", new BigDecimal("12.50"), "USD"),
                new CartItemView("s1", "a1", new BigDecimal("25000"), "VND")));
        when(get.handle(new GetMyCartQuery())).thenReturn(view);
        http.perform(get("/carts/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"id":"cart-1","customerId":"customer-1","items":[
                          {"screeningId":"s2","seatId":"a2","unitPrice":12.50,"currency":"USD"},
                          {"screeningId":"s1","seatId":"a1","unitPrice":25000,"currency":"VND"}
                        ]}
                        """, true));
        verify(get).handle(new GetMyCartQuery());
        verifyNoMoreInteractions(get);
        verifyNoInteractions(add, remove, clear);
    }

    @Test
    void getReturnsHandlerViewUnchanged() {
        CartView view = new CartView("cart-1", "customer-1", List.of());
        when(get.handle(new GetMyCartQuery())).thenReturn(view);
        assertSame(view, controller.getMyCart());
        verify(get).handle(new GetMyCartQuery());
        verifyNoMoreInteractions(get);
        verifyNoInteractions(add, remove, clear);
    }
}
