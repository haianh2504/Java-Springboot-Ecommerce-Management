package checkout.controller;

import checkout.dto.CheckoutRequest;
import checkout.service.CheckoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import order.dto.response.OrderResponse;
import order.entities.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
public class CheckoutController {
    private final CheckoutService checkoutService;

    public CheckoutController(
            CheckoutService checkoutService
    ) {
        this.checkoutService = checkoutService;
    }

    // CHECKOUT by cartId and userId
    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<OrderResponse> checkoutCart(
            @PathVariable @Positive Long cartId,
            @RequestBody @Valid CheckoutRequest request
    )
    {
        Order checkedOutOrder = checkoutService.checkout(request.userId(), cartId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderResponse.from(checkedOutOrder));
    }


}
