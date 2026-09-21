package cart_item.service;

import cart_item.entities.CartItem;
import cart_item.repository.CartItemRepository;
import exception.business.detailed_exceptions.CartItemAlreadyExistsException;
import exception.business.detailed_exceptions.InsufficientStockException;
import exception.business.detailed_exceptions.ProductInactiveException;
import exception.resource.detailed_exceptions.CartItemNotFoundException;
import exception.resource.detailed_exceptions.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.entities.Product;
import product.entities.name.ProductName;
import product.entities.ProductStatus;
import product.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartItemManagementServiceImplTest {
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private CartItemManagementServiceImpl cartItemManagementServiceImpl;

    private CartItem createPersistedCartItem()
    {
        return new CartItem(1L, 10L, 100L, 2);
    }

    private Product createPersistedActiveProduct()
    {
        return new Product(
                100L,
                new ProductName("Mechanical Keyboard"),
                10,
                new BigDecimal("89.99"),
                ProductStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    // Get cartItem list with persisted cartId provided
    @Test
    @DisplayName("Get all cart items for a persisted cart successfully")
    void getCartItemsByCartId_persistedCart_returnsCartItems()
    {
        // --GIVEN--
        Long cartId = 10L;
        List<CartItem> persistedCartItems = List.of(
                createPersistedCartItem(),
                new CartItem(2L, cartId, 101L, 1)
        );
        when(cartItemRepository.findByCartId(cartId)).thenReturn(persistedCartItems);

        // --WHEN--
        List<CartItem> actualCartItems = cartItemManagementServiceImpl.getCartItemsByCartId(cartId);

        // --THEN--
        assertAll(
                () -> assertEquals(persistedCartItems, actualCartItems),
                () -> assertEquals(2, actualCartItems.size()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> actualCartItems.add(createPersistedCartItem()))
        );
        verify(cartItemRepository).findByCartId(cartId);
        verifyNoInteractions(productRepository);
    }

    // Get cartItem list with null cart Id
    @Test
    @DisplayName("Get cart items with a null cart ID throws NullPointerException")
    void getCartItemsByCartId_nullCartId_throwsNullPointerException()
    {
        // --GIVEN--
        Long cartId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartItemManagementServiceImpl.getCartItemsByCartId(cartId)
        );

        // --THEN--
        assertEquals("cartId cannot be null", exception.getMessage());
        verifyNoInteractions(cartItemRepository, productRepository);
    }

    // Get cartItem with valid and persisted cart and product Id, return persisted cartItem
    @Test
    @DisplayName("Get a cart item with persisted cart and product IDs successfully")
    void getCartItemByCartIdAndProductId_validIds_returnsPersistedCartItem()
    {
        // --GIVEN--
        CartItem persistedCartItem = createPersistedCartItem();
        Long cartId = persistedCartItem.getCartId();
        Long productId = persistedCartItem.getProductId();
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.of(persistedCartItem));

        // --WHEN--
        CartItem actualCartItem = cartItemManagementServiceImpl
                .getCartItemByCartIdAndProductId(cartId, productId);

        // --THEN--
        assertSame(persistedCartItem, actualCartItem);
        verify(cartItemRepository).findByCartIdAndProductId(cartId, productId);
        verifyNoInteractions(productRepository);
    }

    // Get cartItem by valid cartId and productId, but cartItemNotFound -> throw exception
    @Test
    @DisplayName("Get a missing cart item by cart and product IDs throws CartItemNotFoundException")
    void getCartItemByCartIdAndProductId_missingCartItem_throwsCartItemNotFoundException()
    {
        // --GIVEN--
        Long cartId = 10L;
        Long productId = 100L;
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.empty());

        // --WHEN--
        CartItemNotFoundException exception = assertThrows(
                CartItemNotFoundException.class,
                () -> cartItemManagementServiceImpl
                        .getCartItemByCartIdAndProductId(cartId, productId)
        );

        // --THEN--
        assertEquals(
                "Cart item with cart [id=10] and product [id=100] not found",
                exception.getMessage()
        );
        verify(cartItemRepository).findByCartIdAndProductId(cartId, productId);
        verifyNoInteractions(productRepository);
    }

    // Get cartItem with null arguments provided, throw null pointer exception ( using ParameterizedTest )
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullGetCartItemArguments")
    @DisplayName("Get a cart item with a null required ID throws NullPointerException")
    void getCartItemByCartIdAndProductId_nullArgument_throwsNullPointerException(
            String nullArgument, Long cartId, Long productId, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullGetCartItemArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartItemManagementServiceImpl
                        .getCartItemByCartIdAndProductId(cartId, productId)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(cartItemRepository, productRepository);
    }

    static Stream<Arguments> nullGetCartItemArguments()
    {
        return Stream.of(
                Arguments.of("cart ID", null, 100L, "cartId cannot be null"),
                Arguments.of("product ID", 10L, null, "productId cannot be null")
        );
    }

    // Add new cartItem in a persisted cart with valid arguments successfully
    @Test
    @DisplayName("Add a new cart item with valid arguments, then save and return it")
    void addNewCartItem_validArguments_savesAndReturnsCartItem()
    {
        // --GIVEN--
        Long cartId = 10L;
        Product product = createPersistedActiveProduct();
        Long productId = product.getId();
        int number = 2;
        CartItem savedCartItem = createPersistedCartItem();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedCartItem);

        // --WHEN--
        CartItem actualCartItem = cartItemManagementServiceImpl
                .addNewCartItem(cartId, productId, number);

        // --THEN--
        assertSame(savedCartItem, actualCartItem);
        verify(productRepository).findById(productId);
        verify(cartItemRepository).findByCartIdAndProductId(cartId, productId);
        verify(cartItemRepository).save(argThat(cartItem ->
                cartItem.getCartItemId() == null
                        && cartItem.getCartId().equals(cartId)
                        && cartItem.getProductId().equals(productId)
                        && cartItem.getNumber() == number
        ));
    }

    // Add new cartItem in a persisted cart but productId not found -> throw exception
    @Test
    @DisplayName("Add a cart item with an unknown product ID throws ProductNotFoundException")
    void addNewCartItem_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long cartId = 10L;
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> cartItemManagementServiceImpl.addNewCartItem(cartId, productId, 2)
        );

        // --THEN--
        assertEquals("Product with id 999 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartItemRepository);
    }

    // Add new cartItem but negative and zero quantity -> throw IllegalException
    @ParameterizedTest(name = "{index}: quantity {0}")
    @MethodSource("nonPositiveCartItemQuantities")
    @DisplayName("Add a cart item with a non-positive quantity throws IllegalArgumentException")
    void addNewCartItem_nonPositiveQuantity_throwsIllegalArgumentException(int invalidQuantity)
    {
        // --GIVEN--
        Long cartId = 10L;
        Product product = createPersistedActiveProduct();
        Long productId = product.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartItemManagementServiceImpl
                        .addNewCartItem(cartId, productId, invalidQuantity)
        );

        // --THEN--
        assertEquals("number must be greater than 0", exception.getMessage());
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartItemRepository);
    }

    static Stream<Integer> nonPositiveCartItemQuantities()
    {
        return Stream.of(0, -1);
    }

    // Add new cartItem but invalid quantity -> throw InsufficientStockException
    @Test
    @DisplayName("Add a cart item with quantity above available stock throws InsufficientStockException")
    void addNewCartItem_quantityAboveStock_throwsInsufficientStockException()
    {
        // --GIVEN--
        Long cartId = 10L;
        Product product = createPersistedActiveProduct();
        Long productId = product.getId();
        int requestedQuantity = 11;
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // --WHEN--
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> cartItemManagementServiceImpl
                        .addNewCartItem(cartId, productId, requestedQuantity)
        );

        // --THEN--
        assertEquals(
                "Product [id=100] does not have enough stock: requested 11, available 10",
                exception.getMessage()
        );
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartItemRepository);
    }

    // Add a persisted cartItem -> throw CartItemAlreadyExistsException
    @Test
    @DisplayName("Add an existing cart item throws CartItemAlreadyExistsException")
    void addNewCartItem_existingCartItem_throwsCartItemAlreadyExistsException()
    {
        // --GIVEN--
        CartItem existingCartItem = createPersistedCartItem();
        Long cartId = existingCartItem.getCartId();
        Product product = createPersistedActiveProduct();
        Long productId = product.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .thenReturn(Optional.of(existingCartItem));

        // --WHEN--
        CartItemAlreadyExistsException exception = assertThrows(
                CartItemAlreadyExistsException.class,
                () -> cartItemManagementServiceImpl.addNewCartItem(cartId, productId, 2)
        );

        // --THEN--
        assertEquals(
                "This cartItem has already existed in cart [id=10] with the product [id=100]",
                exception.getMessage()
        );
        verify(productRepository).findById(productId);
        verify(cartItemRepository).findByCartIdAndProductId(cartId, productId);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    // Delete cartItem with valid arguments successfully
    @Test
    @DisplayName("Delete an existing cart item successfully")
    void deleteCartItem_existingCartItem_deletesSuccessfully()
    {
        // --GIVEN--
        CartItem persistedCartItem = createPersistedCartItem();
        Long cartItemId = persistedCartItem.getCartItemId();
        when(cartItemRepository.findByCartItemId(cartItemId))
                .thenReturn(Optional.of(persistedCartItem));

        // --WHEN--
        cartItemManagementServiceImpl.deleteCartItem(cartItemId);

        // --THEN--
        verify(cartItemRepository).findByCartItemId(cartItemId);
        verify(cartItemRepository).deleteByCartItemId(cartItemId);
        verifyNoInteractions(productRepository);
    }

    // Delete cartItem but cartItemId not found -> throw CartItemNotFoundException
    @Test
    @DisplayName("Delete an unknown cart item throws CartItemNotFoundException")
    void deleteCartItem_unknownCartItem_throwsCartItemNotFoundException()
    {
        // --GIVEN--
        Long cartItemId = 999L;
        when(cartItemRepository.findByCartItemId(cartItemId)).thenReturn(Optional.empty());

        // --WHEN--
        CartItemNotFoundException exception = assertThrows(
                CartItemNotFoundException.class,
                () -> cartItemManagementServiceImpl.deleteCartItem(cartItemId)
        );

        // --THEN--
        assertEquals("cart item with id 999 not found", exception.getMessage());
        verify(cartItemRepository).findByCartItemId(cartItemId);
        verify(cartItemRepository, never()).deleteByCartItemId(anyLong());
        verifyNoInteractions(productRepository);
    }

    // Delete cartItem but null cartItemId provided -> throw null exception
    @Test
    @DisplayName("Delete a cart item with a null ID throws NullPointerException")
    void deleteCartItem_nullCartItemId_throwsNullPointerException()
    {
        // --GIVEN--
        Long cartItemId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartItemManagementServiceImpl.deleteCartItem(cartItemId)
        );

        // --THEN--
        assertEquals("cartItemId cannot be null", exception.getMessage());
        verifyNoInteractions(cartItemRepository, productRepository);
    }

    // Update cartItem with valid arguments and update successfully
    @Test
    @DisplayName("Update an existing cart item's quantity successfully")
    void updateCartItemQuantity_validArguments_updatesSuccessfully()
    {
        // --GIVEN--
        CartItem persistedCartItem = createPersistedCartItem();
        Long cartItemId = persistedCartItem.getCartItemId();
        Product product = createPersistedActiveProduct();
        int newQuantity = 5;
        when(cartItemRepository.findByCartItemId(cartItemId))
                .thenReturn(Optional.of(persistedCartItem));
        when(productRepository.findById(persistedCartItem.getProductId()))
                .thenReturn(Optional.of(product));

        // --WHEN--
        CartItem updated = cartItemManagementServiceImpl.updateCartItemQuantity(
                cartItemId,
                newQuantity
        );

        // --THEN--
        assertEquals(newQuantity, persistedCartItem.getNumber());
        assertSame(persistedCartItem, updated);
        verify(cartItemRepository).findByCartItemId(cartItemId);
        verify(productRepository).findById(persistedCartItem.getProductId());
        verify(cartItemRepository).update(persistedCartItem);
    }

    // Update cartItem with productId not found -> throw ProductNotFoundException
    @Test
    @DisplayName("Update a cart item whose product is missing throws ProductNotFoundException")
    void updateCartItemQuantity_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        CartItem persistedCartItem = createPersistedCartItem();
        Long cartItemId = persistedCartItem.getCartItemId();
        Long productId = persistedCartItem.getProductId();
        when(cartItemRepository.findByCartItemId(cartItemId))
                .thenReturn(Optional.of(persistedCartItem));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> cartItemManagementServiceImpl.updateCartItemQuantity(cartItemId, 5)
        );

        // --THEN--
        assertAll(
                () -> assertEquals("Product with id 100 not found", exception.getMessage()),
                () -> assertEquals(2, persistedCartItem.getNumber())
        );
        verify(cartItemRepository).findByCartItemId(cartItemId);
        verify(productRepository).findById(productId);
        verify(cartItemRepository, never()).update(any(CartItem.class));
    }

    // Update cartItem with insufficient quantity -> throw InsufficientStockException
    @Test
    @DisplayName("Update a cart item above available stock throws InsufficientStockException")
    void updateCartItemQuantity_quantityAboveStock_throwsInsufficientStockException()
    {
        // --GIVEN--
        CartItem persistedCartItem = createPersistedCartItem();
        Long cartItemId = persistedCartItem.getCartItemId();
        Product product = createPersistedActiveProduct();
        int requestedQuantity = 11;
        when(cartItemRepository.findByCartItemId(cartItemId))
                .thenReturn(Optional.of(persistedCartItem));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        // --WHEN--
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> cartItemManagementServiceImpl
                        .updateCartItemQuantity(cartItemId, requestedQuantity)
        );

        // --THEN--
        assertAll(
                () -> assertEquals(
                        "Product [id=100] does not have enough stock: requested 11, available 10",
                        exception.getMessage()
                ),
                () -> assertEquals(2, persistedCartItem.getNumber())
        );
        verify(cartItemRepository).findByCartItemId(cartItemId);
        verify(productRepository).findById(product.getId());
        verify(cartItemRepository, never()).update(any(CartItem.class));
    }

    // Update cartItem with the same quantity -> no change happen
    @Test
    @DisplayName("Update a cart item with the same quantity does not call repository update")
    void updateCartItemQuantity_sameQuantity_doesNotUpdate()
    {
        // --GIVEN--
        CartItem persistedCartItem = createPersistedCartItem();
        Long cartItemId = persistedCartItem.getCartItemId();
        Product product = createPersistedActiveProduct();
        int sameQuantity = persistedCartItem.getNumber();
        when(cartItemRepository.findByCartItemId(cartItemId))
                .thenReturn(Optional.of(persistedCartItem));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        // --WHEN--
        CartItem unchanged = cartItemManagementServiceImpl.updateCartItemQuantity(
                cartItemId,
                sameQuantity
        );

        // --THEN--
        assertEquals(sameQuantity, persistedCartItem.getNumber());
        assertSame(persistedCartItem, unchanged);
        verify(cartItemRepository).findByCartItemId(cartItemId);
        verify(productRepository).findById(product.getId());
        verify(cartItemRepository, never()).update(any(CartItem.class));
    }

    // Calculate total price a list cartItems successfully with valid arguments
    @Test
    @DisplayName("Calculate the total price of a valid cart-item list successfully")
    void calculateTotalPrice_validCartItems_returnsCorrectTotal()
    {
        // --GIVEN--
        CartItem firstCartItem = createPersistedCartItem();
        CartItem secondCartItem = new CartItem(2L, 10L, 101L, 3);
        Product firstProduct = createPersistedActiveProduct();
        Product secondProduct = new Product(
                101L,
                new ProductName("Wireless Mouse"),
                20,
                new BigDecimal("20.00"),
                ProductStatus.ACTIVE,
                Instant.parse("2026-09-10T01:00:00Z")
        );
        List<CartItem> cartItems = List.of(firstCartItem, secondCartItem);
        when(productRepository.findById(firstProduct.getId())).thenReturn(Optional.of(firstProduct));
        when(productRepository.findById(secondProduct.getId())).thenReturn(Optional.of(secondProduct));

        // --WHEN--
        BigDecimal actualTotal = cartItemManagementServiceImpl.calculateTotalPrice(cartItems);

        // --THEN--
        assertEquals(0, new BigDecimal("239.98").compareTo(actualTotal));
        verify(productRepository).findById(firstProduct.getId());
        verify(productRepository).findById(secondProduct.getId());
        verifyNoInteractions(cartItemRepository);
    }

    // Calculate total price a list cartItems but their productId not found -> throw ProductNotFoundException
    @Test
    @DisplayName("Calculate a cart total when a product is missing throws ProductNotFoundException")
    void calculateTotalPrice_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        CartItem cartItem = createPersistedCartItem();
        Long productId = cartItem.getProductId();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> cartItemManagementServiceImpl.calculateTotalPrice(List.of(cartItem))
        );

        // --THEN--
        assertEquals("Product with id 100 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartItemRepository);
    }

    // Calculate total price a list cartItems but null arguments provided -> throw nullException
    @Test
    @DisplayName("Calculate a cart total with a null cart-item list throws NullPointerException")
    void calculateTotalPrice_nullCartItemList_throwsNullPointerException()
    {
        // --GIVEN--
        List<CartItem> cartItems = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cartItemManagementServiceImpl.calculateTotalPrice(cartItems)
        );

        // --THEN--
        assertEquals("cartItemList cannot be null", exception.getMessage());
        verifyNoInteractions(cartItemRepository, productRepository);
    }

    // Validate a persisted ACTIVE cartItem with valid quantity
    @Test
    @DisplayName("Validate a cart item for an active product with sufficient stock successfully")
    void validateCartItem_activeProductWithSufficientStock_returnsProduct()
    {
        // --GIVEN--
        CartItem cartItem = createPersistedCartItem();
        Product activeProduct = createPersistedActiveProduct();
        when(productRepository.findById(cartItem.getProductId()))
                .thenReturn(Optional.of(activeProduct));

        // --WHEN--
        Product actualProduct = cartItemManagementServiceImpl
                .validatedCartItemToOrderItem(cartItem);

        // --THEN--
        assertSame(activeProduct, actualProduct);
        verify(productRepository).findById(cartItem.getProductId());
        verifyNoInteractions(cartItemRepository);
    }

    // Validate a persisted INACTIVE cartItem with valid quantity -> throw ProductInactiveException
    @Test
    @DisplayName("Validate a cart item for an inactive product throws ProductInactiveException")
    void validateCartItem_inactiveProduct_throwsProductInactiveException()
    {
        // --GIVEN--
        CartItem cartItem = createPersistedCartItem();
        Product inactiveProduct = createPersistedActiveProduct();
        inactiveProduct.deactivate();
        when(productRepository.findById(cartItem.getProductId()))
                .thenReturn(Optional.of(inactiveProduct));

        // --WHEN--
        ProductInactiveException exception = assertThrows(
                ProductInactiveException.class,
                () -> cartItemManagementServiceImpl.validatedCartItemToOrderItem(cartItem)
        );

        // --THEN--
        assertEquals("This product with id: 100 is inactive", exception.getMessage());
        verify(productRepository).findById(cartItem.getProductId());
        verifyNoInteractions(cartItemRepository);
    }

    // Validate a persisted ACTIVE cartItem with invalid quantity -> throw InsufficientStockException
    @Test
    @DisplayName("Validate a cart item above available product stock throws InsufficientStockException")
    void validateCartItem_quantityAboveStock_throwsInsufficientStockException()
    {
        // --GIVEN--
        CartItem cartItem = new CartItem(2L, 10L, 100L, 11);
        Product activeProduct = createPersistedActiveProduct();
        when(productRepository.findById(cartItem.getProductId()))
                .thenReturn(Optional.of(activeProduct));

        // --WHEN--
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> cartItemManagementServiceImpl.validatedCartItemToOrderItem(cartItem)
        );

        // --THEN--
        assertEquals(
                "Product [id=100] does not have enough stock: requested 11, available 10",
                exception.getMessage()
        );
        verify(productRepository).findById(cartItem.getProductId());
        verifyNoInteractions(cartItemRepository);
    }

    // Validate a cartItem which product not found -> ProductNotFoundException
    @Test
    @DisplayName("Validate a cart item whose product is missing throws ProductNotFoundException")
    void validateCartItem_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        CartItem cartItem = createPersistedCartItem();
        Long productId = cartItem.getProductId();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> cartItemManagementServiceImpl.validatedCartItemToOrderItem(cartItem)
        );

        // --THEN--
        assertEquals("Product with id 100 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verifyNoInteractions(cartItemRepository);
    }



}
