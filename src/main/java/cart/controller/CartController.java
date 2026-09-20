package cart.controller;

import cart.dto.request.CreateCartRequest;
import cart.dto.response.CartResponse;
import cart.entities.Cart;
import cart.service.CartManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {
    private final CartManagementService cartManagementService;

    public CartController(CartManagementService cartManagementService) {
        this.cartManagementService = cartManagementService;
    }

    // CREATE cart -> 201 CREATED
    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            @RequestBody @Valid CreateCartRequest request
    ) {
        Cart createdCart = cartManagementService.createCart(request.userId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartResponse.from(createdCart));
    }
    // GET cart by id -> 200 OK
    @GetMapping("/{id}")
    public ResponseEntity<CartResponse> getCartById(
            @PathVariable @Positive Long id
    )
    {
        Cart cart = cartManagementService.getCartById(id);
        return ResponseEntity.ok(CartResponse.from(cart));
    }

    // GET list cart by userId -> 200 OK
    @GetMapping(params = "userId")
    public ResponseEntity<List<CartResponse>> getListCartByUserId(
            @RequestParam @Positive Long userId
    )
    {
        List<Cart> cartList = cartManagementService.getCartByUserId(userId);
        List<CartResponse> response = cartList.stream()
                .map(cart -> CartResponse.from(cart)) // lấy mỗi phần tử trong stream lần lượt và biến nó thành giá trị khác
                .toList(); // thu thap -> bien thanh List
        return ResponseEntity.ok(response);
    }

    // CLEAR cart -> 204 NO CONTENT
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(
            @PathVariable @Positive Long cartId
    ) {
        cartManagementService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

}
