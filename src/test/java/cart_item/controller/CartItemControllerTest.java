package cart_item.controller;

import cart_item.entities.CartItem;
import cart_item.service.CartItemManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CartItemControllerTest {
    @Mock
    private CartItemManagementService cartItemManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CartItemController(cartItemManagementService)).build();
    }

    @Test
    void validateCartItem_validItem_returnsValidatedProduct() throws Exception {
        Long cartId = 10L;
        Long productId = 20L;
        CartItem cartItem = new CartItem(1L, cartId, productId, 2);
        Product product = new Product(
                productId,
                new ProductName("Mechanical Keyboard"),
                10,
                new BigDecimal("89.99"),
                ProductStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
        when(cartItemManagementService.getCartItemByCartIdAndProductId(cartId, productId))
                .thenReturn(cartItem);
        when(cartItemManagementService.validatedCartItemToOrderItem(cartItem))
                .thenReturn(product);

        mockMvc.perform(get(
                        "/api/v1/cart-items/carts/{cartId}/products/{productId}/validation",
                        cartId,
                        productId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        InOrder inOrder = inOrder(cartItemManagementService);
        inOrder.verify(cartItemManagementService)
                .getCartItemByCartIdAndProductId(cartId, productId);
        inOrder.verify(cartItemManagementService).validatedCartItemToOrderItem(cartItem);
    }
}
