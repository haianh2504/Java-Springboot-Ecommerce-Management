package cart.service;

import cart.entities.Cart;
import cart.entities.CartStatus;
import cart.repository.CartRepository;
import cart_item.repository.CartItemRepository;
import exception.business.detailed_exceptions.AccountBannedException;
import exception.business.detailed_exceptions.CartAlreadyCheckedOutException;
import exception.resource.detailed_exceptions.CartNotFoundException;
import exception.resource.detailed_exceptions.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import user.entities.*;
import user.entities.email.Email;
import user.entities.password_hash.PasswordHash;
import user.entities.person_name.PersonName;
import user.entities.phone_number.PhoneNumber;
import user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartManagementServiceImplTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CartManagementServiceImpl cartManagementServiceImpl;

    private Cart createPersistedActiveCart()
    {
        return new Cart(
                1L,
                10L,
                Instant.parse("2026-09-10T00:00:00Z"),
                CartStatus.ACTIVE
        );
    }

    private Cart createPersistedCheckedOutCart()
    {
        return new Cart(
                2L,
                10L,
                Instant.parse("2026-09-10T01:00:00Z"),
                CartStatus.CHECKED_OUT
        );
    }

    private User createPersistedUser(Long userId, UserStatus status)
    {
        return new User(
                userId,
                new PasswordHash("$2342haHkacnd"),
                new PersonName("Cart Owner"),
                status == UserStatus.PENDING ? null : new PhoneNumber("0912345678"),
                new Email("cart.owner@example.com"),
                UserRole.NORMAL_USER,
                status,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    // Create new cart for a persisted user, save and return saved cart
    @Test
    @DisplayName("Create a cart for an existing user, then save and return the persisted cart")
    void createCart_existingUser_savesAndReturnsPersistedCart()
    {
        // --GIVEN--
        Long userId = 10L;
        Cart savedCart = createPersistedActiveCart();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createPersistedUser(userId, UserStatus.ACTIVE)));
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        // --WHEN--
        Cart actualCart = cartManagementServiceImpl.createCart(userId);

        // --THEN--
        assertAll(
                () -> assertSame(savedCart, actualCart),
                () -> assertEquals(1L, actualCart.getCartId()),
                () -> assertEquals(userId, actualCart.getUserId()),
                () -> assertSame(CartStatus.ACTIVE, actualCart.getCartStatus())
        );
        verify(cartRepository).save(argThat(cart ->
                cart.getCartId() == null
                        && cart.getUserId().equals(userId)
                        && cart.getCreatedAt() != null
                        && cart.getCartStatus() == CartStatus.ACTIVE
        ));
        verify(userRepository).findById(userId);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Create a cart for a pending user")
    void createCart_pendingUser_savesCart()
    {
        Long userId = 10L;
        Cart savedCart = createPersistedActiveCart();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createPersistedUser(userId, UserStatus.PENDING)));
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        Cart actualCart = cartManagementServiceImpl.createCart(userId);

        assertSame(savedCart, actualCart);
        verify(userRepository).findById(userId);
        verify(cartRepository).save(any(Cart.class));
        verifyNoInteractions(cartItemRepository);
    }

    // Create new cart failed due to user not found -> throw exception
    @Test
    @DisplayName("Create a cart when persistence reports a missing user throws UserNotFoundException")
    void createCart_unknownUser_throwsUserNotFoundException()
    {
        // --GIVEN--
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // --WHEN--
        UserNotFoundException actualException = assertThrows(
                UserNotFoundException.class,
                () -> cartManagementServiceImpl.createCart(userId)
        );

        // --THEN--
        assertAll(
                () -> assertEquals("User with id 99 not found", actualException.getMessage())
        );
        verify(userRepository).findById(userId);
        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Create a cart for a banned user throws AccountBannedException")
    void createCart_bannedUser_throwsAccountBannedException()
    {
        Long userId = 10L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(createPersistedUser(userId, UserStatus.BANNED)));

        AccountBannedException exception = assertThrows(
                AccountBannedException.class,
                () -> cartManagementServiceImpl.createCart(userId)
        );

        assertEquals("This account has already been banned.", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(cartRepository, cartItemRepository);
    }

    // Create new cart but null userId -> throw exception
    @Test
    @DisplayName("Create a cart with a null user ID throws NullPointerException")
    void createCart_nullUserId_throwsNullPointerException()
    {
        // --GIVEN--
        Long userId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartManagementServiceImpl.createCart(userId)
        );

        // --THEN--
        assertEquals("userId cannot be null", exception.getMessage());
        verifyNoInteractions(cartRepository, cartItemRepository, userRepository);
    }

    // Get carts successfully by a persisted user's Id
    @Test
    @DisplayName("Get all carts for an existing user successfully")
    void getCartByUserId_existingUser_returnsUserCarts()
    {
        // --GIVEN--
        Long userId = 10L;
        List<Cart> persistedCarts = List.of(
                createPersistedActiveCart(),
                createPersistedCheckedOutCart()
        );
        when(cartRepository.findByUserId(userId)).thenReturn(persistedCarts);

        // --WHEN--
        List<Cart> actualCarts = cartManagementServiceImpl.getCartByUserId(userId);

        // --THEN--
        assertAll(
                () -> assertEquals(persistedCarts, actualCarts),
                () -> assertEquals(2, actualCarts.size()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> actualCarts.add(createPersistedActiveCart()))
        );
        verify(cartRepository).findByUserId(userId);
        verifyNoInteractions(cartItemRepository);
    }

    // Get carts gone wrong due to user not exist
    @Test
    @DisplayName("Get carts for a user with no repository result returns an empty list")
    void getCartByUserId_noCartsFound_returnsEmptyList()
    {
        // --GIVEN--
        Long userId = 99L;
        when(cartRepository.findByUserId(userId)).thenReturn(List.of());

        // --WHEN--
        List<Cart> actualCarts = cartManagementServiceImpl.getCartByUserId(userId);

        // --THEN--
        assertTrue(actualCarts.isEmpty());
        verify(cartRepository).findByUserId(userId);
        verifyNoInteractions(cartItemRepository);
    }

    // Clear cart successfully
    @Test
    @DisplayName("Clear a cart successfully by deleting all of its cart items")
    void clearCart_validCartId_deletesAllCartItems()
    {
        // --GIVEN--
        Long cartId = 1L;

        // --WHEN--
        cartManagementServiceImpl.clearCart(cartId);

        // --THEN--
        verify(cartItemRepository).deleteAllByCartId(cartId);
        verifyNoInteractions(cartRepository);
    }

    // Clear cart with null cartId provided -> throw exception
    @Test
    @DisplayName("Clear a cart with a null cart ID throws NullPointerException")
    void clearCart_nullCartId_throwsNullPointerException()
    {
        // --GIVEN--
        Long cartId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartManagementServiceImpl.clearCart(cartId)
        );

        // --THEN--
        assertEquals("cartId cannot be null", exception.getMessage());
        verifyNoInteractions(cartRepository, cartItemRepository);
    }

    // Check out cart successfully
    @Test
    @DisplayName("Atomically claim an existing active cart for checkout")
    void checkoutCart_existingActiveCart_claimsSuccessfully()
    {
        // --GIVEN--
        Cart persistedCart = createPersistedActiveCart();
        Long cartId = persistedCart.getCartId();
        when(cartRepository.markCheckedOutIfActive(cartId)).thenReturn(true);

        // --WHEN--
        cartManagementServiceImpl.checkoutCart(cartId);

        // --THEN--
        assertSame(CartStatus.ACTIVE, persistedCart.getCartStatus());
        verify(cartRepository).markCheckedOutIfActive(cartId);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Check out an already checked-out cart throws CartAlreadyCheckedOutException")
    void checkoutCart_checkedOutCart_throwsCartAlreadyCheckedOutException()
    {
        // --GIVEN--
        Cart persistedCart = createPersistedCheckedOutCart();
        Long cartId = persistedCart.getCartId();
        when(cartRepository.markCheckedOutIfActive(cartId)).thenReturn(false);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(persistedCart));

        // --WHEN--
        CartAlreadyCheckedOutException exception = assertThrows(
                CartAlreadyCheckedOutException.class,
                () -> cartManagementServiceImpl.checkoutCart(cartId)
        );

        // --THEN--
        assertEquals("This cart has already been checked out", exception.getMessage());
        verify(cartRepository).markCheckedOutIfActive(cartId);
        verify(cartRepository).findById(cartId);
        verifyNoInteractions(cartItemRepository);
    }

    // Check out cart but cart not found -> throw exception
    @Test
    @DisplayName("Check out an unknown cart throws CartNotFoundException")
    void checkoutCart_unknownCart_throwsCartNotFoundException()
    {
        // --GIVEN--
        Long cartId = 99L;
        when(cartRepository.markCheckedOutIfActive(cartId)).thenReturn(false);
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        // --WHEN--
        CartNotFoundException exception = assertThrows(
                CartNotFoundException.class,
                () -> cartManagementServiceImpl.checkoutCart(cartId)
        );

        // --THEN--
        assertEquals("Cart with id 99 not found", exception.getMessage());
        verify(cartRepository).markCheckedOutIfActive(cartId);
        verify(cartRepository).findById(cartId);
        verifyNoInteractions(cartItemRepository);
    }

    // Check out cart but null cartId provided
    @Test
    @DisplayName("Check out a cart with a null cart ID throws NullPointerException")
    void checkoutCart_nullCartId_throwsNullPointerException()
    {
        // --GIVEN--
        Long cartId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartManagementServiceImpl.checkoutCart(cartId)
        );

        // --THEN--
        assertEquals("cartId cannot be null", exception.getMessage());
        verifyNoInteractions(cartRepository, cartItemRepository);
    }
}
