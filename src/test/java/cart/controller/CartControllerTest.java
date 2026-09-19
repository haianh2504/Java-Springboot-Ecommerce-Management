package cart.controller;

import cart.entities.Cart;
import cart.entities.CartStatus;
import cart.service.CartManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
    @Mock
    private CartManagementService cartManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CartController(cartManagementService)).build();
    }

    @Test
    void getListCartByUserId_existingCarts_returnsCartResponses() throws Exception {
        Long userId = 10L;
        List<Cart> carts = List.of(
                new Cart(
                        1L,
                        userId,
                        Instant.parse("2026-09-10T00:00:00Z"),
                        CartStatus.ACTIVE
                ),
                new Cart(
                        2L,
                        userId,
                        Instant.parse("2026-09-11T00:00:00Z"),
                        CartStatus.CHECKED_OUT
                )
        );
        when(cartManagementService.getCartByUserId(userId)).thenReturn(carts);

        mockMvc.perform(get("/api/v1/carts").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartId").value(1))
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].cartId").value(2))
                .andExpect(jsonPath("$[1].status").value("CHECKED_OUT"));

        verify(cartManagementService).getCartByUserId(userId);
    }

    @Test
    void getListCartByUserId_noCarts_returnsEmptyList() throws Exception {
        Long userId = 10L;
        when(cartManagementService.getCartByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/carts").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(cartManagementService).getCartByUserId(userId);
    }

    @Test
    void clearCart_validCartId_returnsNoContent() throws Exception {
        Long cartId = 1L;

        mockMvc.perform(delete("/api/v1/carts/{cartId}/items", cartId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cartManagementService).clearCart(cartId);
    }

    @Test
    void checkoutCart_validCartId_returnsNoContent() throws Exception {
        Long cartId = 1L;

        mockMvc.perform(patch("/api/v1/carts/{cartId}/checkout", cartId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cartManagementService).checkoutCart(cartId);
    }
}
