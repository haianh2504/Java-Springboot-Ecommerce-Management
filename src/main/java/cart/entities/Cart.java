package cart.entities;

import cart_item.entities.CartItem;
import exception.business.detailed_exceptions.CartAlreadyCheckedOutException;

import java.time.Instant;
import java.util.*;

@Entity
@Tables()
public class Cart{
    private Long cartId;
    private Long userId;
    private Instant createdAt;
    private CartStatus cartStatus;
//    constructor for SQL return
    public Cart(Long cartId, Long userId, Instant createdAt,CartStatus cartStatus) // with list Items
    {
        this.cartId = Objects.requireNonNull(cartId,"cartId cannot be null");
        this.userId = Objects.requireNonNull(userId,"userId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Timestamp createdAt cannot be null");
        this.cartStatus = Objects.requireNonNull(cartStatus,"cartStatus cannot be null");
    }
//    constructor for creating new one
    public Cart(Long userId)
    {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.createdAt = Instant.now();
        this.cartStatus = CartStatus.ACTIVE;
    }
//    getters
    public final Long getCartId()
    {
        return this.cartId;
    }
    public final Long getUserId()
    {
        return this.userId;
    }
    public final Instant getCreatedAt() {return this.createdAt;}
    public final CartStatus getCartStatus()
    {
        return this.cartStatus;
    }
//    setter
    public final void setCheckedOutStatus()
    {
        Objects.requireNonNull(cartStatus,"cartStatus cannot be null");
        if(this.cartStatus == CartStatus.CHECKED_OUT){
            throw new CartAlreadyCheckedOutException();
        }
        this.cartStatus = CartStatus.CHECKED_OUT;
    }
}
