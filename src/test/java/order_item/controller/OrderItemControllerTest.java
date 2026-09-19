package order_item.controller;

import order_item.entities.OrderItem;
import order_item.service.OrderItemManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class OrderItemControllerTest {
    private static final Long ORDER_ID = 10L;
    private static final Long PRODUCT_ID = 20L;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("15.50");

    @Mock
    private OrderItemManagementService orderItemManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new OrderItemController(orderItemManagementService)).build();
    }

    private OrderItem persistedOrderItem() {
        return new OrderItem(1L, ORDER_ID, PRODUCT_ID, 2, UNIT_PRICE);
    }

    @Test
    void createOrderItem_validRequest_returnsCreatedOrderItem() throws Exception {
        when(orderItemManagementService.createNewOrderItem(
                ORDER_ID, PRODUCT_ID, 2, UNIT_PRICE
        )).thenReturn(persistedOrderItem());

        mockMvc.perform(post("/api/v1/orders/{orderId}/items", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 20,
                                  "quantity": 2,
                                  "unitPrice": 15.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderItemId").value(1))
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.productId").value(20))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(15.50))
                .andExpect(jsonPath("$.totalPrice").value(31.00));

        verify(orderItemManagementService).createNewOrderItem(
                ORDER_ID, PRODUCT_ID, 2, UNIT_PRICE
        );
    }

    @Test
    void deleteOrderItem_existingItem_returnsNoContent() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/orders/{orderId}/items/{productId}",
                        ORDER_ID,
                        PRODUCT_ID
                ))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(orderItemManagementService).deleteOrderItem(ORDER_ID, PRODUCT_ID);
    }

    @Test
    void getOrderItem_existingItem_returnsResponse() throws Exception {
        when(orderItemManagementService.getOrderItem(ORDER_ID, PRODUCT_ID))
                .thenReturn(persistedOrderItem());

        mockMvc.perform(get(
                        "/api/v1/orders/{orderId}/items/{productId}",
                        ORDER_ID,
                        PRODUCT_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId").value(1))
                .andExpect(jsonPath("$.totalPrice").value(31.00));

        verify(orderItemManagementService).getOrderItem(ORDER_ID, PRODUCT_ID);
    }

    @Test
    void getOrderItems_existingOrder_returnsResponses() throws Exception {
        when(orderItemManagementService.getOrderItemsByOrderId(ORDER_ID))
                .thenReturn(List.of(persistedOrderItem()));

        mockMvc.perform(get("/api/v1/orders/{orderId}/items", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderItemId").value(1))
                .andExpect(jsonPath("$[0].productId").value(20));

        verify(orderItemManagementService).getOrderItemsByOrderId(ORDER_ID);
    }

    @Test
    void getOrderItemTotalPrice_existingItem_returnsTotal() throws Exception {
        when(orderItemManagementService.getTotalPrice(ORDER_ID, PRODUCT_ID))
                .thenReturn(new BigDecimal("31.00"));

        mockMvc.perform(get(
                        "/api/v1/orders/{orderId}/items/{productId}/total-price",
                        ORDER_ID,
                        PRODUCT_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.productId").value(20))
                .andExpect(jsonPath("$.totalPrice").value(31.00));

        verify(orderItemManagementService).getTotalPrice(ORDER_ID, PRODUCT_ID);
    }
}
