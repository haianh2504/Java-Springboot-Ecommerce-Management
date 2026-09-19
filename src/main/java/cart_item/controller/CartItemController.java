package cart_item.controller;

import cart.dto.response.CartTotalPriceResponse;
import cart_item.dto.request.CreateCartItemRequest;
import cart_item.dto.request.UpdateCartItemQuantityRequest;
import cart_item.dto.response.CartItemResponse;
import cart_item.entities.CartItem;
import cart_item.service.CartItemManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import product.dto.response.ProductResponse;
import product.entities.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cart-items")
public class CartItemController {
    private final CartItemManagementService cartItemManagementService;
    public CartItemController(CartItemManagementService cartItemManagementService) {
        this.cartItemManagementService = cartItemManagementService;
    }
    // ADD new cart -> 201 CREATED
    @PostMapping("/carts/{cartId}/items")
    public ResponseEntity<CartItemResponse> createCartItem(
            @PathVariable @Positive Long cartId,
            @RequestBody @Valid CreateCartItemRequest request
            ) {
        CartItem newCartItem = cartItemManagementService.addNewCartItem(
                cartId,
                request.productId(),
                request.quantity()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CartItemResponse.from(newCartItem));
    }

    // DELETE cart -> 204 NO CONTENT
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> deleteCartItem(
            @PathVariable @Positive Long cartItemId
    )
    {
        cartItemManagementService.deleteCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    // GET list cartItems by cartId -> 200 OK
    @GetMapping("/carts/{cartId}")
    public ResponseEntity<List<CartItemResponse>> getListCartItemsByCartId(
            @PathVariable @Positive Long cartId
    )
    {
        List<CartItem> cartItems = cartItemManagementService.getCartItemsByCartId(cartId);
        List<CartItemResponse> cartItemResponses = cartItems.stream()
                .map(cartItem -> CartItemResponse.from(cartItem))
                .toList();
        return ResponseEntity.ok(cartItemResponses);
    }
    // GET by cartId and productId -> 200 OK
    @GetMapping("/carts/{cartId}/products/{productId}")
    public ResponseEntity<CartItemResponse> getCartItemsByCartIdAndProductId(
            @PathVariable @Positive Long cartId,
            @PathVariable @Positive Long productId
    )
    {
        CartItem cartItem = cartItemManagementService.getCartItemByCartIdAndProductId(cartId, productId);
        return ResponseEntity.ok(CartItemResponse.from(cartItem));
    }

    // UPDATE (PATCH) quantity by id -> 200 OK
    @PatchMapping("/{id}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateCartItemQuantityRequest request
            )
    {
        CartItem cartItem = cartItemManagementService.updateCartItemQuantity(id, request.quantity());
        return ResponseEntity.ok(CartItemResponse.from(cartItem));
    }

    // GET total price of list cart items
    @GetMapping("/carts/{cartId}/total-price")
    public ResponseEntity<CartTotalPriceResponse> getCartTotalPriceByCartId(
            @PathVariable @Positive Long cartId
    )
    {
        List<CartItem> cartItems = cartItemManagementService.getCartItemsByCartId(cartId);
        BigDecimal totalPrice = cartItemManagementService.calculateTotalPrice(cartItems);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CartTotalPriceResponse(cartId,totalPrice));
    }
    // VALIDATE cart item before converting it to an order item
    @GetMapping("/carts/{cartId}/products/{productId}/validation")
    public ResponseEntity<ProductResponse> validateCartItem(
            @PathVariable @Positive Long cartId,
            @PathVariable @Positive Long productId
    ) {
        CartItem cartItem = cartItemManagementService
                .getCartItemByCartIdAndProductId(cartId, productId);
        Product validatedProduct = cartItemManagementService
                .validatedCartItemToOrderItem(cartItem);
        return ResponseEntity.ok(ProductResponse.from(validatedProduct));
    }

}
