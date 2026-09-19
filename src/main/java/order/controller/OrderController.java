package order.controller;

import checkout.service.CheckoutService;
import jakarta.validation.Valid;
import order.dto.request.CreateOrderRequest;
import order.dto.response.OrderResponse;
import order.entities.Order;
import order.service.OrderManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public final class OrderController {
    private final CheckoutService checkoutService;
    private final OrderManagementService orderManagementService;

    public OrderController(
            CheckoutService checkoutService,
            OrderManagementService orderManagementService
    ) {
        this.checkoutService = checkoutService;
        this.orderManagementService = orderManagementService;
    }
    // CREATE order -> 201 CREATED
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request
    ) {
        Order newOrder = checkoutService.checkout(request.userId(), request.cartId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderResponse.from(newOrder));
    }
    // GET by id -> 200 OK
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable @Positive Long orderId
    ) {
        Order order = orderManagementService.getOrderById(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // GET list orders by User Id -> 200 OK
    @GetMapping(params = "userId")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @RequestParam @Positive Long userId
    ) {
        List<OrderResponse> response = orderManagementService
                .getAllOrdersByUserId(userId)
                .stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    // DELETE by id -> 204 NO CONTENT
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrderById(
            @PathVariable @Positive Long orderId
    ) {
        orderManagementService.deleteOrderById(orderId);
        return ResponseEntity.noContent().build();
    }
}
