package cinema.domain.cart;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CartTest {
    private static final Money PRICE = new Money(BigDecimal.TEN, Currency.getInstance("USD"));

    private CartItem item(String screening, String seat) {
        return new CartItem(screening, seat, PRICE);
    }

    private Cart cart(List<CartItem> items) {
        return new Cart("cart-1", "customer-1", items);
    }

    @Test
    void constructorRetainsState() {
        CartItem first = item("s1", "a1");
        Cart cart = cart(List.of(first));
        assertEquals("cart-1", cart.getId());
        assertEquals("customer-1", cart.getCustomerId());
        assertEquals(List.of(first), cart.getItems());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidId(String id) {
        assertThrows(DomainException.class, () -> new Cart(id, "customer-1", List.of()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidCustomerId(String id) {
        assertThrows(DomainException.class, () -> new Cart("cart-1", id, List.of()));
    }

    @Test
    void rejectsNullItems() {
        assertThrows(DomainException.class, () -> cart(null));
    }

    @Test
    void rejectsNullElement() {
        assertThrows(DomainException.class, () -> cart(Arrays.asList(item("s1", "a1"), null)));
    }

    @Test
    void constructorRejectsDuplicatePairEvenWithDifferentPrice() {
        CartItem duplicate = new CartItem("s1", "a1",
                new Money(BigDecimal.ONE, Currency.getInstance("USD")));
        assertThrows(DomainException.class, () -> cart(List.of(item("s1", "a1"), duplicate)));
    }

    @Test
    void allowsSameSeatInDifferentScreeningsAndDifferentSeatsInSameScreening() {
        List<CartItem> items = List.of(item("s1", "a1"), item("s2", "a1"), item("s1", "a2"));
        assertEquals(items, cart(items).getItems());
        Cart cart = cart(List.of());
        items.forEach(cart::addItem);
        assertEquals(items, cart.getItems());
    }

    @Test
    void addsToCartConstructedWithImmutableList() {
        CartItem first = item("s1", "a1");
        CartItem second = item("s1", "a2");
        Cart cart = cart(List.of(first));
        cart.addItem(second);
        assertEquals(List.of(first, second), cart.getItems());
    }

    @Test
    void rejectsNullAdditionWithoutChangingState() {
        Cart cart = cart(List.of(item("s1", "a1")));
        List<CartItem> before = cart.getItems();
        assertThrows(DomainException.class, () -> cart.addItem(null));
        assertEquals(before, cart.getItems());
    }

    @Test
    void rejectsDuplicateAdditionRegardlessOfPriceWithoutChangingState() {
        Cart cart = cart(List.of(item("s1", "a1")));
        List<CartItem> before = cart.getItems();
        CartItem duplicate = new CartItem("s1", "a1",
                new Money(BigDecimal.ONE, Currency.getInstance("USD")));
        assertThrows(DomainException.class, () -> cart.addItem(duplicate));
        assertEquals(before, cart.getItems());
    }

    @Test
    void removalMatchesBothIds() {
        CartItem otherScreening = item("s2", "a1");
        CartItem otherSeat = item("s1", "a2");
        Cart cart = cart(List.of(item("s1", "a1"), otherScreening, otherSeat));
        cart.removeItem("s1", "a1");
        assertEquals(List.of(otherScreening, otherSeat), cart.getItems());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void removalRejectsInvalidScreeningId(String id) {
        Cart cart = cart(List.of(item("s1", "a1")));
        assertThrows(DomainException.class, () -> cart.removeItem(id, "a1"));
        assertEquals(List.of(item("s1", "a1")), cart.getItems());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void removalRejectsInvalidSeatId(String id) {
        Cart cart = cart(List.of(item("s1", "a1")));
        assertThrows(DomainException.class, () -> cart.removeItem("s1", id));
        assertEquals(List.of(item("s1", "a1")), cart.getItems());
    }

    @Test
    void removalRejectsMissingPairWithoutChangingState() {
        List<CartItem> items = List.of(item("s1", "a1"), item("s2", "a2"));
        Cart cart = cart(items);
        assertThrows(DomainException.class, () -> cart.removeItem("s1", "a2"));
        assertEquals(items, cart.getItems());
        assertThrows(DomainException.class, () -> cart(List.of()).removeItem("s1", "a1"));
    }

    @Test
    void clearRemovesAllItemsAndIsSafeWhenEmpty() {
        Cart cart = cart(List.of(item("s1", "a1"), item("s2", "a2")));
        cart.clear();
        assertTrue(cart.getItems().isEmpty());
        assertDoesNotThrow(cart::clear);
        assertTrue(cart.getItems().isEmpty());
        cart.addItem(item("s1", "a1"));
        assertEquals(List.of(item("s1", "a1")), cart.getItems());
    }

    @Test
    void constructorDefensivelyCopiesInput() {
        CartItem first = item("s1", "a1");
        List<CartItem> input = new ArrayList<>(List.of(first));
        Cart cart = cart(input);
        input.clear();
        assertEquals(List.of(first), cart.getItems());
        cart.addItem(item("s2", "a2"));
        assertTrue(input.isEmpty());
    }

    @Test
    void returnedItemsAreImmutableSnapshotsAcrossMutations() {
        CartItem first = item("s1", "a1");
        Cart cart = cart(List.of(first));
        List<CartItem> snapshot = cart.getItems();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(item("s2", "a2")));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(first));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.set(0, item("s2", "a2")));
        cart.addItem(item("s2", "a2"));
        cart.removeItem("s1", "a1");
        cart.clear();
        assertEquals(List.of(first), snapshot);
        assertTrue(cart.getItems().isEmpty());
    }
}
