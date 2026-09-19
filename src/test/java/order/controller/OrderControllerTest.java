package order.controller;

import checkout.service.CheckoutService;
import order.entities.Order;
import order.entities.OrderStatus;
import order.service.OrderManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long CART_ID = 20L;

    @Mock
    private CheckoutService checkoutService;
    @Mock
    private OrderManagementService orderManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(
                new OrderController(checkoutService, orderManagementService)
        ).build();
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
    void createOrder_validRequest_returnsCreatedOrder() throws Exception {
        when(checkoutService.checkout(USER_ID, CART_ID)).thenReturn(persistedOrder());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 10, "cartId": 20}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.cartId").value(20));

        verify(checkoutService).checkout(USER_ID, CART_ID);
    }

    @Test
    void getOrderById_existingOrder_returnsOrder() throws Exception {
        when(orderManagementService.getOrderById(ORDER_ID)).thenReturn(persistedOrder());

        mockMvc.perform(get("/api/v1/orders/{orderId}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.orderStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice").value(105.00));

        verify(orderManagementService).getOrderById(ORDER_ID);
    }

    @Test
    void getOrdersByUserId_existingOrders_returnsList() throws Exception {
        when(orderManagementService.getAllOrdersByUserId(USER_ID))
                .thenReturn(List.of(persistedOrder()));

        mockMvc.perform(get("/api/v1/orders").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].userId").value(10));

        verify(orderManagementService).getAllOrdersByUserId(USER_ID);
    }

    @Test
    void deleteOrderById_existingOrder_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/{orderId}", ORDER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(orderManagementService).deleteOrderById(ORDER_ID);
    }
}
