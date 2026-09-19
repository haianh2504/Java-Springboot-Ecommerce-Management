package cart_item.service;

import cart_item.entities.CartItem;
import product.entities.Product;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemManagementService {
//    get cartItems by cartId
    public List<CartItem> getCartItemsByCartId(Long cartId);

//    add new cartItem in cart
    public CartItem addNewCartItem(Long cartId,Long productId, int number);

//    delete cartItem in cart
    public void deleteCartItem(Long cartItemId);

//    find CartItem by cart ID and product ID in cart
    public CartItem getCartItemByCartIdAndProductId(Long cartId,Long productId);

//    update Cart Item quantity
    public CartItem updateCartItemQuantity(Long cartItemId, int newQuantity);

//    calculate total price for a cartItem
    public BigDecimal calculateTotalPrice(List<CartItem> cartItemList);

//    validate cart item
    public Product validatedCartItemToOrderItem(CartItem cartItem);
}
