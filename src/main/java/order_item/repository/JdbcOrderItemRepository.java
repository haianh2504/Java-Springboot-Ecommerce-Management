package order_item.repository;

import order_item.entities.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcOrderItemRepository implements OrderItemRepository {
    private final Connection connection;
    public JdbcOrderItemRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Order Items' Connection cannot be null");
    }
//    save order item
    @Override
    public void save(OrderItem orderItem) {
        String sql = """
                INSERT INTO order_items(
                order_id,
                product_id,
                quantity,
                unit_price 
                )
                VALUES ( ?, ?, ?, ? );
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,orderItem.getOrderId());
            ps.setLong(2, orderItem.getProductId());
            ps.setInt(3, orderItem.getQuantity());
            ps.setBigDecimal(4, orderItem.getUnitPrice());
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while saving order item: " + e.getMessage(), e);
        }
    }
//    find order item
    public Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId) {
        String sql = """
                SELECT
                id,
                order_id,
                product_id,
                quantity,
                unit_price
                FROM order_items WHERE order_id=? AND product_id=? ;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,orderId);
            ps.setLong(2,productId);
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                return Optional.of(
                        new OrderItem(
                                rs.getLong("id"),
                                rs.getLong("order_id"),
                                rs.getLong("product_id"),
                                rs.getInt("quantity"),
                                rs.getBigDecimal("unit_price")
                        )
                );
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while finding order item: " + e.getMessage(), e);
        }
    }
//    get list order items by orderId
    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = """
                SELECT
                id,
                order_id,
                product_id,
                quantity,
                unit_price
                FROM order_items WHERE order_id=?;
                """;
        List<OrderItem> orderItems = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,orderId);
            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next()){
                    orderItems.add(
                            new OrderItem(
                                    rs.getLong("id"),
                                    rs.getLong("order_id"),
                                    rs.getLong("product_id"),
                                    rs.getInt("quantity"),
                                    rs.getBigDecimal("unit_price")
                            )
                    );
                }
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while finding order items: " + e.getMessage(), e);
        }
        return orderItems;
    }

    //    delete order item
    @Override
    public void delete(Long orderId, Long productId) {
        String sql = """
                DELETE FROM order_items WHERE order_id=? AND product_id=? ;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,orderId);
            ps.setLong(2,productId);
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while deleting order item: " + e.getMessage(), e);
        }
    }
}
