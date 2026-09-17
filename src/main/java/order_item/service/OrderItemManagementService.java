package order_item.service;

import order_item.entities.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemManagementService {
//    create new OrderItem
    public OrderItem createNewOrderItem(Long orderId, Long productId, int quantity, BigDecimal unit_price);

//    delete OrderItem
    public void deleteOrderItem(Long orderId, Long productId);

//    get OrderItem
    public OrderItem getOrderItem(Long orderId, Long productId);

//    get list of order items by orderId
    public List<OrderItem> getOrderItemsByOrderId(Long orderId);

//    calculate total price of an order items
    public BigDecimal getTotalPrice(OrderItem orderItem);
}
