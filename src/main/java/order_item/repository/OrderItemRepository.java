package order_item.repository;

import order_item.entities.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
//    save Order item
    public void save(OrderItem orderItem);
//    delete Order item while Order are in PENDING_PAYMENT status
    public void delete(Long orderId, Long productId);
//    get Order item
    public Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
//    get list order items by orderId
    public List<OrderItem> findByOrderId(Long orderId);
//    update orderItem
}
