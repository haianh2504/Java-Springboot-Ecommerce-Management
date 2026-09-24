package cart.entities;

import common.exception.business.detailed_exceptions.CartAlreadyCheckedOutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private static final Long CART_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final Instant STORED_CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    // @Nested groups tests by the Cart operation whose business rules they verify.
    @Nested
    @DisplayName("Loading a saved cart from database data")
    class LoadSavedCartFromDatabase {
        // @ParameterizedTest runs the same rule check once for each supplied cart status.
        @ParameterizedTest(name = "loads a saved cart with {0} status")
        // @EnumSource supplies every declared CartStatus value and keeps this test current if the enum grows.
        @EnumSource(CartStatus.class)
        @DisplayName("Should load all saved cart values returned by the database")
        void loadSavedCart_validDatabaseValues_preservesAllValues(CartStatus status) {
            Cart cart = new Cart(CART_ID, USER_ID, STORED_CREATED_AT, status);

            assertAll(
                    () -> assertEquals(CART_ID, cart.getCartId()),
                    () -> assertEquals(USER_ID, cart.getUserId()),
                    () -> assertEquals(STORED_CREATED_AT, cart.getCreatedAt()),
                    () -> assertSame(status, cart.getCartStatus())
            );
        }

        // @ParameterizedTest avoids duplicating the same null-invariant assertion for every constructor field.
        @ParameterizedTest(name = "rejects a null {0}")
        // @MethodSource obtains the argument sets from invalidSavedCartDatabaseArguments below.
        @MethodSource("invalidSavedCartDatabaseArguments")
        @DisplayName("Should reject a saved cart when required database data is missing")
        void loadSavedCart_missingRequiredDatabaseValue_throwsNullPointerException(
                String fieldName,
                String expectedMessage,
                Long cartId,
                Long userId,
                Instant createdAt,
                CartStatus status
        ) {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Cart(cartId, userId, createdAt, status)
            );

            assertEquals(expectedMessage, exception.getMessage(), fieldName);
        }

        static Stream<Arguments> invalidSavedCartDatabaseArguments() {
            return Stream.of(
                    Arguments.of("cartId", "cartId cannot be null", null, USER_ID, STORED_CREATED_AT, CartStatus.ACTIVE),
                    Arguments.of("userId", "userId cannot be null", CART_ID, null, STORED_CREATED_AT, CartStatus.ACTIVE),
                    Arguments.of("createdAt", "Timestamp createdAt cannot be null", CART_ID, USER_ID, null, CartStatus.ACTIVE),
                    Arguments.of("cartStatus", "cartStatus cannot be null", CART_ID, USER_ID, STORED_CREATED_AT, null)
            );
        }
    }

    // @Nested keeps new-cart rules separate from persistence-constructor rules.
    @Nested
    @DisplayName("Creating a new cart")
    class CreateCart {

        @Test
        @DisplayName("Should create an active cart for a valid user")
        void newCart_validUser_createsActiveCart() {
            Instant beforeCreation = Instant.now();

            Cart cart = new Cart(USER_ID);

            Instant afterCreation = Instant.now();
            assertAll(
                    () -> assertNull(cart.getCartId()),
                    () -> assertEquals(USER_ID, cart.getUserId()),
                    () -> assertSame(CartStatus.ACTIVE, cart.getCartStatus()),
                    () -> assertFalse(cart.getCreatedAt().isBefore(beforeCreation)),
                    () -> assertFalse(cart.getCreatedAt().isAfter(afterCreation))
            );
        }

        @Test
        @DisplayName("Should reject a new cart without a user")
        void newCart_nullUserId_throwsNullPointerException() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Cart((Long) null)
            );

            assertEquals("User ID cannot be null", exception.getMessage());
        }
    }

    // @Nested isolates checkout state-transition scenarios.
    @Nested
    @DisplayName("Checking out a cart")
    class CheckOutCart {

        @Test
        @DisplayName("Should change an active cart to checked out")
        void setCheckedOutStatus_activeCart_changesStatus() {
            Cart cart = savedCartWithStatus(CartStatus.ACTIVE);

            cart.setCheckedOutStatus();

            assertSame(CartStatus.CHECKED_OUT, cart.getCartStatus());
        }

        @Test
        @DisplayName("Should reject checking out an already checked-out cart")
        void setCheckedOutStatus_checkedOutCart_throwsCartAlreadyCheckedOutException() {
            Cart cart = savedCartWithStatus(CartStatus.CHECKED_OUT);

            CartAlreadyCheckedOutException exception = assertThrows(
                    CartAlreadyCheckedOutException.class,
                    cart::setCheckedOutStatus
            );

            assertAll(
                    () -> assertEquals("This cart has already been checked out", exception.getMessage()),
                    () -> assertSame(CartStatus.CHECKED_OUT, cart.getCartStatus())
            );
        }

        // GIẢ LẬP DỮ LIỆU Ở DB BỊ SỬA THÀNH NULL
        @Test
        @DisplayName("Should reject checkout when persisted state has been corrupted") // from DB usually
        void setCheckedOutStatus_nullStatus_throwsNullPointerException() throws ReflectiveOperationException {
            Cart cart = savedCartWithStatus(CartStatus.ACTIVE);
            setCartStatusForCorruptionTest(cart, null);

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    cart::setCheckedOutStatus
            );

            assertAll(
                    () -> assertEquals("cartStatus cannot be null", exception.getMessage()),
                    () -> assertNull(cart.getCartStatus())
            );
        }
        // PHƯƠNG PHÁP PHÒNG THỦ SỐ 2 SAU LỚP SỐ 1 ( BUSINESS RULES IN CONSTRUCTOR )
    }

    // CONVENIENCE FUNCTIONS
    private static Cart savedCartWithStatus(CartStatus status) {
        return new Cart(CART_ID, USER_ID, STORED_CREATED_AT, status);
    }

    private static void setCartStatusForCorruptionTest(Cart cart, CartStatus status)
            throws ReflectiveOperationException {
        Field cartStatus = Cart.class.getDeclaredField("cartStatus");
        cartStatus.setAccessible(true);
        cartStatus.set(cart, status);
    }

}
