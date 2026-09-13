package checkout.entities;

import cart_item.entities.CartItem;
import product.entities.Product;

import java.math.BigDecimal;
import java.util.Objects;

public record CheckoutItem(CartItem cartItem, Product product) {
    public CheckoutItem{
        Objects.requireNonNull(cartItem, "cartItem cannot be null");
        Objects.requireNonNull(product, "product cannot be null");
    }

    public BigDecimal unitPrice() {
        return product.getBasePrice();
    }

    public BigDecimal lineTotal() {
        return unitPrice().multiply(BigDecimal.valueOf(cartItem.getNumber()));
    }
}
