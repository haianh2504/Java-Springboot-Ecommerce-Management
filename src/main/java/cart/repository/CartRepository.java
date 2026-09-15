package cart.repository;

import cart.entities.Cart;
import cart_item.entities.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartRepository {
//    Find cart by id
    public Optional<Cart> findById(Long id);
//    Find cart by userId
    public List<Cart> findByUserId(Long userId);
//    save cart
    public Cart save(Cart cart);
//    delete cart
    public void deleteById(Long cartId);
//    update cart
    public void update(Cart cart);
    // Atomically claims an active cart for checkout. The status change participates
    // in the caller's JDBC transaction, so a later rollback restores ACTIVE.
    public boolean markCheckedOutIfActive(Long cartId);
}
