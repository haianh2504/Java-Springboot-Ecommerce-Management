package order_item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import order_item.dto.request.CreateOrderItemRequest;
import order_item.dto.response.OrderItemResponse;
import order_item.dto.response.OrderItemTotalPriceResponse;
import order_item.entities.OrderItem;
import order_item.service.OrderItemManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/items")
public class OrderItemController {
    private final OrderItemManagementService orderItemManagementService;

    public OrderItemController(OrderItemManagementService orderItemManagementService) {
        this.orderItemManagementService = orderItemManagementService;
    }

    // CREATE new order Item -> 201 CREATED
    @PostMapping
    public ResponseEntity<OrderItemResponse> createOrderItem(
            @PathVariable @Positive Long orderId,
            @RequestBody @Valid CreateOrderItemRequest request
    ) {
        OrderItem createdOrderItem = orderItemManagementService.createNewOrderItem(
                orderId,
                request.productId(),
                request.quantity(),
                request.unitPrice()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderItemResponse.from(createdOrderItem));
    }

    // DELETE orderItem by Order and product Id -> 204 NO CONTENT
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteOrderItem(
            @PathVariable @Positive Long orderId,
            @PathVariable @Positive Long productId
    ) {
        orderItemManagementService.deleteOrderItem(orderId, productId);
        return ResponseEntity.noContent().build();
    }

    // GET by order and product Id -> 200 OK
    @GetMapping("/{productId}")
    public ResponseEntity<OrderItemResponse> getOrderItem(
            @PathVariable @Positive Long orderId,
            @PathVariable @Positive Long productId
    ) {
        OrderItem orderItem = orderItemManagementService.getOrderItem(orderId, productId);
        return ResponseEntity.ok(OrderItemResponse.from(orderItem));
    }

    // GET List order item by Order Id -> 200 OK
    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(
            @PathVariable @Positive Long orderId
    ) {
        List<OrderItemResponse> response = orderItemManagementService
                .getOrderItemsByOrderId(orderId)
                .stream()
                .map(orderItem -> OrderItemResponse.from(orderItem))
                .toList();
        return ResponseEntity.ok(response);
    }

    // GET totalPrice by order and product Id -> 200 OK
    @GetMapping("/{productId}/total-price")
    public ResponseEntity<OrderItemTotalPriceResponse> getOrderItemTotalPrice(
            @PathVariable @Positive Long orderId,
            @PathVariable @Positive Long productId
    ) {
        BigDecimal totalPrice = orderItemManagementService.getTotalPrice(orderId, productId);
        return ResponseEntity.ok(
                new OrderItemTotalPriceResponse(orderId, productId, totalPrice)
        );
    }
}
