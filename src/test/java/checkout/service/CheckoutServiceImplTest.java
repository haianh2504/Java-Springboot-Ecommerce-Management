package checkout.service;

import cart.entities.Cart;
import cart.entities.CartStatus;
import cart.service.CartManagementService;
import cart_item.entities.CartItem;
import cart_item.service.CartItemManagementService;
import discount.service.DiscountService;
import exception.business.detailed_exceptions.CartIsEmptyException;
import exception.business.detailed_exceptions.CartOwnershipMismatchException;
import exception.business.detailed_exceptions.InsufficientStockException;
import exception.resource.detailed_exceptions.CartNotFoundException;
import order.entities.Order;
import order.entities.OrderStatus;
import order.service.OrderManagementService;
import order_item.service.OrderItemManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.entities.Product;
import product.entities.name.ProductName;
import product.entities.ProductStatus;
import product.service.ProductManagementService;
import shipping.ShippingStrategy;
import transaction_management.TransactionManagement;
import transaction_management.TransactionWork;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {
    private static final Long USER_ID = 10L;
    private static final Long CART_ID = 20L;
    private static final Long ORDER_ID = 30L;
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("30.00");
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("20.00");

    @Mock
    private CartManagementService cartManagementService;
    @Mock
    private CartItemManagementService cartItemManagementService;
    @Mock
    private ShippingStrategy shippingStrategy;
    @Mock
    private DiscountService discountService;
    @Mock
    private OrderManagementService orderManagementService;
    @Mock
    private OrderItemManagementService orderItemManagementService;
    @Mock
    private ProductManagementService productManagementService;
    @Mock
    private TransactionManagement transactionManagement;

    @InjectMocks
    private CheckoutServiceImpl checkoutServiceImpl;

    @Test
    @DisplayName("Checkout with valid cart data completes the pricing and mutation workflow in order")
    void checkout_validCart_completesWorkflowInOrder()
    {
        // --GIVEN--
        executeTransactionWorkImmediately();
        Cart cart = createActiveCart(USER_ID);
        CartItem firstCartItem = new CartItem(1L, CART_ID, 101L, 2);
        CartItem secondCartItem = new CartItem(2L, CART_ID, 102L, 1);
        List<CartItem> cartItems = List.of(firstCartItem, secondCartItem);
        Product firstProduct = createProduct(101L, "Keyboard", "100.00", 10);
        Product secondProduct = createProduct(102L, "E-book", "50.00", 20);
        BigDecimal expectedSubTotal = new BigDecimal("250.00");
        BigDecimal expectedTotal = new BigDecimal("260.00");
        Order persistedOrder = createPersistedOrder(
                expectedSubTotal, SHIPPING_FEE, DISCOUNT_AMOUNT, expectedTotal
        );

        when(cartManagementService.getCartById(CART_ID)).thenReturn(cart);
        when(cartItemManagementService.getCartItemsByCartId(CART_ID)).thenReturn(cartItems);
        when(cartItemManagementService.validatedCartItemToOrderItem(firstCartItem))
                .thenReturn(firstProduct);
        when(cartItemManagementService.validatedCartItemToOrderItem(secondCartItem))
                .thenReturn(secondProduct);
        when(shippingStrategy.calculateShippingFee(any())).thenReturn(SHIPPING_FEE);
        when(discountService.calculateDiscountAmount(expectedSubTotal)).thenReturn(DISCOUNT_AMOUNT);
        when(orderManagementService.createOrder(
                USER_ID, CART_ID, expectedSubTotal, SHIPPING_FEE, DISCOUNT_AMOUNT, expectedTotal
        )).thenReturn(persistedOrder);

        // --WHEN--
        Order actualOrder = checkoutServiceImpl.checkout(USER_ID, CART_ID);

        // --THEN--
        assertSame(persistedOrder, actualOrder);
        verify(transactionManagement).execute(any());
        verify(shippingStrategy).calculateShippingFee(argThat(items ->
                items.size() == 2
                        && items.get(0).cartItem() == firstCartItem
                        && items.get(0).product() == firstProduct
                        && items.get(1).cartItem() == secondCartItem
                        && items.get(1).product() == secondProduct
        ));
        verify(discountService).calculateDiscountAmount(expectedSubTotal);
        verify(orderManagementService).createOrder(
                USER_ID, CART_ID, expectedSubTotal, SHIPPING_FEE, DISCOUNT_AMOUNT, expectedTotal
        );

        InOrder mutationOrder = inOrder(
                cartManagementService,
                orderManagementService,
                orderItemManagementService,
                productManagementService,
                cartManagementService
        );
        mutationOrder.verify(cartManagementService).checkoutCart(CART_ID);
        mutationOrder.verify(orderManagementService).createOrder(
                USER_ID, CART_ID, expectedSubTotal, SHIPPING_FEE, DISCOUNT_AMOUNT, expectedTotal
        );
        mutationOrder.verify(orderItemManagementService).createNewOrderItem(
                ORDER_ID, 101L, 2, new BigDecimal("100.00")
        );
        mutationOrder.verify(orderItemManagementService).createNewOrderItem(
                ORDER_ID, 102L, 1, new BigDecimal("50.00")
        );
        mutationOrder.verify(productManagementService).decreaseStockQuantity(101L, 2);
        mutationOrder.verify(productManagementService).decreaseStockQuantity(102L, 1);
    }

    @Test
    @DisplayName("Checkout validates a null user ID before starting a transaction")
    void checkout_nullUserId_throwsBeforeTransaction()
    {
        // --GIVEN--
        Long userId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> checkoutServiceImpl.checkout(userId, CART_ID)
        );

        // --THEN--
        assertEquals("userId is required", exception.getMessage());
        verifyNoInteractions(transactionManagement);
    }

    @Test
    @DisplayName("Checkout validates a null cart ID before starting a transaction")
    void checkout_nullCartId_throwsBeforeTransaction()
    {
        // --GIVEN--
        Long cartId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> checkoutServiceImpl.checkout(USER_ID, cartId)
        );

        // --THEN--
        assertEquals("cartId is required", exception.getMessage());
        verifyNoInteractions(transactionManagement);
    }

    @Test
    @DisplayName("Checkout of a missing cart stops before loading cart items")
    void checkout_unknownCart_throwsCartNotFoundException()
    {
        // --GIVEN--
        executeTransactionWorkImmediately();
        when(cartManagementService.getCartById(CART_ID)).thenThrow(new CartNotFoundException(CART_ID));

        // --WHEN--
        CartNotFoundException exception = assertThrows(
                CartNotFoundException.class,
                () -> checkoutServiceImpl.checkout(USER_ID, CART_ID)
        );

        // --THEN--
        assertEquals("Cart with id 20 not found", exception.getMessage());
        verify(cartManagementService).getCartById(CART_ID);
        verifyNoInteractions(cartItemManagementService);
        verifyNoMutationInteractions();
    }

    @Test
    @DisplayName("Checkout of another user's cart stops before loading cart items")
    void checkout_cartOwnershipMismatch_throwsCartOwnershipMismatchException()
    {
        // --GIVEN--
        executeTransactionWorkImmediately();
        Cart cart = createActiveCart(99L);
        when(cartManagementService.getCartById(CART_ID)).thenReturn(cart);

        // --WHEN--
        CartOwnershipMismatchException exception = assertThrows(
                CartOwnershipMismatchException.class,
                () -> checkoutServiceImpl.checkout(USER_ID, CART_ID)
        );

        // --THEN--
        assertNotNull(exception.getMessage());
        verify(cartManagementService).getCartById(CART_ID);
        verifyNoInteractions(cartItemManagementService);
        verifyNoMutationInteractions();
    }

    @Test
    @DisplayName("Checkout of an empty cart stops before product validation and mutations")
    void checkout_emptyCart_throwsCartIsEmptyException()
    {
        // --GIVEN--
        executeTransactionWorkImmediately();
        when(cartManagementService.getCartById(CART_ID)).thenReturn(createActiveCart(USER_ID));
        when(cartItemManagementService.getCartItemsByCartId(CART_ID)).thenReturn(List.of());

        // --WHEN--
        CartIsEmptyException exception = assertThrows(
                CartIsEmptyException.class,
                () -> checkoutServiceImpl.checkout(USER_ID, CART_ID)
        );

        // --THEN--
        assertNotNull(exception.getMessage());
        verify(cartItemManagementService).getCartItemsByCartId(CART_ID);
        verifyNoInteractions(shippingStrategy, discountService);
        verifyCartWasReservedWithoutOrderMutations();
    }

    @Test
    @DisplayName("Insufficient stock during validation stops checkout before pricing and mutations")
    void checkout_insufficientStockDuringValidation_stopsBeforeMutations()
    {
        // --GIVEN--
        executeTransactionWorkImmediately();
        CartItem cartItem = new CartItem(1L, CART_ID, 101L, 5);
        when(cartManagementService.getCartById(CART_ID)).thenReturn(createActiveCart(USER_ID));
        when(cartItemManagementService.getCartItemsByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartItemManagementService.validatedCartItemToOrderItem(cartItem))
                .thenThrow(new InsufficientStockException(101L, 5, 2));

        // --WHEN--
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> checkoutServiceImpl.checkout(USER_ID, CART_ID)
        );

        // --THEN--
        assertEquals(
                "Product [id=101] does not have enough stock: requested 5, available 2",
                exception.getMessage()
        );
        verifyNoInteractions(shippingStrategy, discountService);
        verifyCartWasReservedWithoutOrderMutations();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidPriceAdjustments")
    @DisplayName("Checkout rejects invalid shipping or discount values before writing an order")
    void checkout_invalidPriceAdjustment_stopsBeforeOrderMutation(
            String scenario,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            Class<? extends RuntimeException> exceptionType,
            String expectedMessage
    ) {
        // --GIVEN--
        executeTransactionWorkImmediately();
        CartItem cartItem = new CartItem(1L, CART_ID, 101L, 1);
        Product product = createProduct(101L, "E-book", "100.00", 5);
        when(cartManagementService.getCartById(CART_ID)).thenReturn(createActiveCart(USER_ID));
        when(cartItemManagementService.getCartItemsByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartItemManagementService.validatedCartItemToOrderItem(cartItem)).thenReturn(product);
        when(shippingStrategy.calculateShippingFee(any())).thenReturn(shippingFee);
        when(discountService.calculateDiscountAmount(new BigDecimal("100.00")))
                .thenReturn(discountAmount);

        // --WHEN--
        RuntimeException exception = assertThrows(
                exceptionType,
                () -> checkoutServiceImpl.checkout(USER_ID, CART_ID),
                scenario
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyCartWasReservedWithoutOrderMutations();
    }

    static Stream<Arguments> invalidPriceAdjustments()
    {
        return Stream.of(
                Arguments.of("null shipping", null, BigDecimal.ZERO,
                        NullPointerException.class, "shippingFee is required"),
                Arguments.of("negative shipping", new BigDecimal("-0.01"), BigDecimal.ZERO,
                        IllegalArgumentException.class, "shippingFee cannot be negative"),
                Arguments.of("null discount", BigDecimal.ZERO, null,
                        NullPointerException.class, "discountAmount is required"),
                Arguments.of("negative discount", BigDecimal.ZERO, new BigDecimal("-0.01"),
                        IllegalArgumentException.class, "discountAmount cannot be negative"),
                Arguments.of("discount exceeds payable amount", BigDecimal.ZERO, new BigDecimal("100.01"),
                        IllegalArgumentException.class, "discountAmount cannot exceed the payable amount")
        );
    }

    @Test
    @DisplayName("Atomic stock-decrease failure rolls back and prevents the cart from becoming checked out")
    void checkout_stockDecreaseFails_rollsBackAndDoesNotCheckoutCart() throws SQLException
    {
        // --GIVEN--
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        CheckoutServiceImpl checkoutServiceWithRealTransaction = new CheckoutServiceImpl(
                cartManagementService,
                cartItemManagementService,
                shippingStrategy,
                discountService,
                orderManagementService,
                orderItemManagementService,
                productManagementService,
                new TransactionManagement(connection)
        );
        CartItem cartItem = new CartItem(1L, CART_ID, 101L, 2);
        Product product = createProduct(101L, "E-book", "50.00", 2);
        BigDecimal subTotal = new BigDecimal("100.00");
        Order persistedOrder = createPersistedOrder(
                subTotal, BigDecimal.ZERO, BigDecimal.ZERO, subTotal
        );
        when(cartManagementService.getCartById(CART_ID)).thenReturn(createActiveCart(USER_ID));
        when(cartItemManagementService.getCartItemsByCartId(CART_ID)).thenReturn(List.of(cartItem));
        when(cartItemManagementService.validatedCartItemToOrderItem(cartItem)).thenReturn(product);
        when(shippingStrategy.calculateShippingFee(any())).thenReturn(BigDecimal.ZERO);
        when(discountService.calculateDiscountAmount(subTotal)).thenReturn(BigDecimal.ZERO);
        when(orderManagementService.createOrder(
                USER_ID, CART_ID, subTotal, BigDecimal.ZERO, BigDecimal.ZERO, subTotal
        )).thenReturn(persistedOrder);
        doThrow(new InsufficientStockException(101L, 2, 1))
                .when(productManagementService).decreaseStockQuantity(101L, 2);

        // --WHEN--
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> checkoutServiceWithRealTransaction.checkout(USER_ID, CART_ID)
        );

        // --THEN--
        assertNotNull(exception.getMessage());
        verify(orderItemManagementService).createNewOrderItem(
                ORDER_ID, 101L, 2, new BigDecimal("50.00")
        );
        verify(productManagementService).decreaseStockQuantity(101L, 2);
        verify(cartManagementService).checkoutCart(CART_ID);
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(true);
    }

    private void executeTransactionWorkImmediately()
    {
        doAnswer(invocation -> {
            TransactionWork<?> work = invocation.getArgument(0);
            return work.execute();
        }).when(transactionManagement).execute(any());
    }

    private void verifyNoMutationInteractions()
    {
        verifyNoInteractions(
                orderManagementService,
                orderItemManagementService,
                productManagementService
        );
        verify(cartManagementService, never()).checkoutCart(anyLong());
    }

    private void verifyCartWasReservedWithoutOrderMutations()
    {
        verify(cartManagementService).checkoutCart(CART_ID);
        verifyNoInteractions(
                orderManagementService,
                orderItemManagementService,
                productManagementService
        );
    }

    private Cart createActiveCart(Long ownerId)
    {
        return new Cart(
                CART_ID,
                ownerId,
                Instant.parse("2026-09-10T00:00:00Z"),
                CartStatus.ACTIVE
        );
    }

    private Product createProduct(Long id, String name, String price, int stockQuantity)
    {
        return new Product(
                id,
                new ProductName(name),
                stockQuantity,
                new BigDecimal(price),
                ProductStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    private Order createPersistedOrder(
            BigDecimal subTotal,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalPrice
    )
    {
        return new Order(
                ORDER_ID,
                USER_ID,
                CART_ID,
                OrderStatus.PENDING_PAYMENT,
                Instant.parse("2026-09-10T00:00:00Z"),
                subTotal,
                shippingFee,
                discountAmount,
                totalPrice
        );
    }
}
