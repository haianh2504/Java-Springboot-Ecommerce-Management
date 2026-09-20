package checkout.controller;

import checkout.service.CheckoutService;
import order.entities.Order;
import order.entities.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long CART_ID = 20L;

    @Mock
    private CheckoutService checkoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new CheckoutController(checkoutService)).build();
    }

    private Order persistedOrder() {
        return new Order(
                ORDER_ID,
                USER_ID,
                CART_ID,
                OrderStatus.PENDING_PAYMENT,
                Instant.parse("2026-09-10T00:00:00Z"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("105.00")
        );
    }

    @Test
    void checkoutCart_validRequest_returnsCreatedOrder() throws Exception {
        when(checkoutService.checkout(USER_ID, CART_ID)).thenReturn(persistedOrder());

        mockMvc.perform(post("/api/v1/carts/{cartId}/checkout", CART_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.cartId").value(20))
                .andExpect(jsonPath("$.orderStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice").value(105.00));

        verify(checkoutService).checkout(USER_ID, CART_ID);
    }
}
