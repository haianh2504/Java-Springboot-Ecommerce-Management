package order.repository;

import order.entities.Order;
import order.entities.OrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcOrderRepository implements OrderRepository {
    private final Connection connection;
//    constructor
    public JdbcOrderRepository(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection cannot be null");
    }
//    save order
    public Order save(Order order) {
        String sql = """
                INSERT INTO orders(
                user_id,
                cart_id,
                status,
                created_at,
                sub_total,
                shipping_fee,
                discount_amount,
                total_price
                )
                VALUES(?,?,?,?,?,?,?,?)
                RETURNING id
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, order.getUserId());
            ps.setLong(2, order.getCartId());
            ps.setString(3,order.getOrderStatus().name());
            ps.setTimestamp(4, java.sql.Timestamp.from(order.getCreatedAt()));
            ps.setBigDecimal(5, order.getSubTotal());
            ps.setBigDecimal(6, order.getShippingFee());
            ps.setBigDecimal(7, order.getDiscountAmount());
            ps.setBigDecimal(8, order.getTotalPrice());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    Long generatedId = rs.getLong("id");
                    return new Order(
                            generatedId,
                            order.getUserId(),
                            order.getCartId(),
                            order.getOrderStatus(),
                            order.getCreatedAt(),
                            order.getSubTotal(),
                            order.getShippingFee(),
                            order.getDiscountAmount(),
                            order.getTotalPrice()
                    );
                }
                return null;
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while saving order: " + e.getMessage(),e);
        }
    }
//    get orders by userId
    @Override
    public List<Order> findByUserId(Long userId) {
        String sql = """
                SELECT * FROM orders WHERE user_id = ?;
        """;
        List<Order> orders = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,userId);
            try(ResultSet rs = ps.executeQuery()){ // try-with-resource
                while(rs.next())
                {
                    orders.add(
                            new Order(
                                    rs.getLong("id"),
                                    rs.getLong("user_id"),
                                    rs.getLong("cart_id"),
                                    OrderStatus.valueOf(rs.getString("status")),
                                    rs.getTimestamp("created_at").toInstant(),
                                    rs.getBigDecimal("sub_total"),
                                    rs.getBigDecimal("shipping_fee"),
                                    rs.getBigDecimal("discount_amount"),
                                    rs.getBigDecimal("total_price")
                            )
                    );
                }
            }
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while fetching orders: " + e.getMessage(),e);
        }
        return orders;
    }
//    get order by orderId
    @Override
    public Optional<Order> findByOrderId(Long orderId) {
        String sql = """
                SELECT * FROM orders WHERE id = ?;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
           ps.setLong(1,orderId);
           try(ResultSet rs = ps.executeQuery();){
               if(!rs.next())
               {
                   return Optional.empty();
               }
               return Optional.of(
                       new Order(
                               rs.getLong("id"),
                               rs.getLong("user_id"),
                               rs.getLong("cart_id"),
                               OrderStatus.valueOf(rs.getString("status")),
                               rs.getTimestamp("created_at").toInstant(),
                               rs.getBigDecimal("sub_total"),
                               rs.getBigDecimal("shipping_fee"),
                               rs.getBigDecimal("discount_amount"),
                               rs.getBigDecimal("total_price")
                       )
               );
           }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while fetching orders: " + e.getMessage(),e);
        }
    }
//    delete Order by orderId
    @Override
    public void deleteByOrderId(Long orderId) {
        String sql = """
                DELETE FROM orders WHERE id = ?;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1,orderId);
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while deleting orders: " + e.getMessage(),e);
        }
    }
//    update order
    @Override
    public void update(Order order) {
        String sql = """
                UPDATE orders
                SET 
                status = ?,
                sub_total = ?,
                shipping_fee = ?,
                discount_amount = ?,
                total_price = ?
                WHERE id = ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, order.getOrderStatus().name());
            ps.setBigDecimal(2, order.getSubTotal());
            ps.setBigDecimal(3, order.getShippingFee());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setBigDecimal(5, order.getTotalPrice());
            ps.setLong(6, order.getOrderId());
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while fetching orders: " + e.getMessage(),e);
        }
    }
//    find by user and cart id
    @Override
    public Optional<Order> findByUserIdAndCartId(Long userId, Long cartId) {
        String sql = """
                SELECT * FROM orders WHERE user_id = ? AND cart_id = ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
         ps.setLong(1, userId);
         ps.setLong(2, cartId);
         try(ResultSet rs = ps.executeQuery()){
             if(!rs.next())
             {
                 return Optional.empty();
             }
             return Optional.of(
                     new Order(
                             rs.getLong("id"),
                             rs.getLong("user_id"),
                             rs.getLong("cart_id"),
                             OrderStatus.valueOf(rs.getString("status")),
                             rs.getTimestamp("created_at").toInstant(),
                             rs.getBigDecimal("sub_total"),
                             rs.getBigDecimal("shipping_fee"),
                             rs.getBigDecimal("discount_amount"),
                             rs.getBigDecimal("total_price")
                     )
             );
         }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching orders by user and cart id: " + e.getMessage(),e);
        }
    }
}
