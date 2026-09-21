package cart_item.controller;

import cart_item.entities.CartItem;
import cart_item.service.CartItemManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import product.entities.Product;
import product.entities.name.ProductName;
import product.entities.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CartItemControllerTest {
    private static final Long CART_ID = 10L;
    private static final Long CART_ITEM_ID = 1L;
    private static final Long PRODUCT_ID = 20L;

    @Mock
    private CartItemManagementService cartItemManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CartItemController(cartItemManagementService)).build();
    }

    private CartItem persistedCartItem() {
        return new CartItem(CART_ITEM_ID, CART_ID, PRODUCT_ID, 2);
    }

    @Test
    void createCartItem_validRequest_returnsCreatedItem() throws Exception {
        when(cartItemManagementService.addNewCartItem(CART_ID, PRODUCT_ID, 2))
                .thenReturn(persistedCartItem());

        mockMvc.perform(post("/api/v1/carts/{cartId}/items", CART_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":20,\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cartItemId").value(1))
                .andExpect(jsonPath("$.cartId").value(10))
                .andExpect(jsonPath("$.productId").value(20))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(cartItemManagementService).addNewCartItem(CART_ID, PRODUCT_ID, 2);
    }

    @Test
    void deleteCartItem_existingItem_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/cart-items/{cartItemId}", CART_ITEM_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cartItemManagementService).deleteCartItem(CART_ITEM_ID);
    }

    @Test
    void getCartItems_existingCart_returnsItems() throws Exception {
        when(cartItemManagementService.getCartItemsByCartId(CART_ID))
                .thenReturn(List.of(persistedCartItem()));

        mockMvc.perform(get("/api/v1/carts/{cartId}/items", CART_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartItemId").value(1))
                .andExpect(jsonPath("$[0].productId").value(20));

        verify(cartItemManagementService).getCartItemsByCartId(CART_ID);
    }

    @Test
    void getCartItem_existingCartAndProduct_returnsItem() throws Exception {
        when(cartItemManagementService.getCartItemByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(persistedCartItem());

        mockMvc.perform(get("/api/v1/carts/{cartId}/items/{productId}", CART_ID, PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").value(1))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(cartItemManagementService)
                .getCartItemByCartIdAndProductId(CART_ID, PRODUCT_ID);
    }

    @Test
    void updateCartItemQuantity_validRequest_returnsUpdatedItem() throws Exception {
        CartItem updated = new CartItem(CART_ITEM_ID, CART_ID, PRODUCT_ID, 4);
        when(cartItemManagementService.updateCartItemQuantity(CART_ITEM_ID, 4))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/v1/cart-items/{cartItemId}", CART_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartItemId").value(1))
                .andExpect(jsonPath("$.quantity").value(4));

        verify(cartItemManagementService).updateCartItemQuantity(CART_ITEM_ID, 4);
    }

    @Test
    void getCartTotalPrice_existingCart_returnsTotal() throws Exception {
        List<CartItem> items = List.of(persistedCartItem());
        when(cartItemManagementService.getCartItemsByCartId(CART_ID)).thenReturn(items);
        when(cartItemManagementService.calculateTotalPrice(items))
                .thenReturn(new BigDecimal("179.98"));

        mockMvc.perform(get("/api/v1/carts/{cartId}/items/total-price", CART_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(10))
                .andExpect(jsonPath("$.totalPrice").value(179.98));

        InOrder inOrder = inOrder(cartItemManagementService);
        inOrder.verify(cartItemManagementService).getCartItemsByCartId(CART_ID);
        inOrder.verify(cartItemManagementService).calculateTotalPrice(items);
    }

    @Test
    void validateCartItem_validItem_returnsValidatedProduct() throws Exception {
        CartItem cartItem = persistedCartItem();
        Product product = new Product(
                PRODUCT_ID,
                new ProductName("Mechanical Keyboard"),
                10,
                new BigDecimal("89.99"),
                ProductStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
        when(cartItemManagementService.getCartItemByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(cartItem);
        when(cartItemManagementService.validatedCartItemToOrderItem(cartItem))
                .thenReturn(product);

        mockMvc.perform(get(
                        "/api/v1/carts/{cartId}/items/{productId}/validation",
                        CART_ID,
                        PRODUCT_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        InOrder inOrder = inOrder(cartItemManagementService);
        inOrder.verify(cartItemManagementService)
                .getCartItemByCartIdAndProductId(CART_ID, PRODUCT_ID);
        inOrder.verify(cartItemManagementService).validatedCartItemToOrderItem(cartItem);
    }
}
