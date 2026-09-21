package order.service;

import exception.resource.detailed_exceptions.OrderNotFoundException;
import order.entities.Order;
import order.entities.OrderStatus;
import order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class OrderManagementServiceImpl implements OrderManagementService {
    private final OrderRepository orderRepository;
//    constructor
    public OrderManagementServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
    }
//    create Order
    public Order createOrder(Long userId,
                             Long cartId,
                             BigDecimal subTotal,
                             BigDecimal shippingFee,
                             BigDecimal discountAmount,
                             BigDecimal totalPrice) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(cartId, "cartId must not be null");
        Objects.requireNonNull(subTotal, "subTotal must not be null");
        Objects.requireNonNull(shippingFee, "shippingFee must not be null");
        Objects.requireNonNull(discountAmount, "discountAmount must not be null");
        Objects.requireNonNull(totalPrice, "totalPrice must not be null");
        if(subTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("subtotal cannot be negative");
        }
        if(discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("discountAmount cannot be negative");
        }
        if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("ShippingFee cannot be negative");
        }
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalPrice cannot be negative");
        }
        Order order = new Order(userId,cartId, subTotal, shippingFee, discountAmount, totalPrice);
        return orderRepository.save(order);

    }
//    get list of orders by userId
    @Override
    public List<Order> getAllOrdersByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return orderRepository.findByUserId(userId);
    }
//    delete order by id
    public void deleteOrderById(Long orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        if(orderRepository.findByOrderId(orderId).isEmpty()){
            throw new OrderNotFoundException(orderId);
        }
        orderRepository.deleteByOrderId(orderId);
    }
//    get order by order id
    @Override
    public Order getOrderById(Long orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );
    }

}
