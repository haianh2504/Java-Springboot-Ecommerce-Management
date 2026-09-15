package cart.repository;

import cart.entities.Cart;
import cart.entities.CartStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcCartRepository implements CartRepository {
    private final Connection connection;
//    constructor
    public JdbcCartRepository(Connection connection){
        this.connection = Objects.requireNonNull(connection, "Database connection undefined");
    }
//    Find cart by id
    @Override
    public Optional<Cart> findById(Long id) {
        String sql = """
                SELECT
                id,
                user_id,
                created_at,
                status
                FROM carts WHERE id = ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            // add key word into ?
            ps.setLong(1, id);
            // activate sql query
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                return Optional.of(
                        new Cart(
                                rs.getLong("id"),
                                rs.getLong("user_id"),
                                rs.getTimestamp("created_at").toInstant(),
                                CartStatus.valueOf(rs.getString("status"))
                        )
                );
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while searching cart by id: " + e.getMessage(), e);
        }
    }
//    Find cart by userId
    @Override
    public List<Cart> findByUserId(Long userId) {
        String sql = """
                SELECT
                id,
                user_id,
                created_at,
                status
                FROM carts WHERE user_id = ?;
                """;
        List<Cart> carts = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, userId);
            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    carts.add(
                            new Cart(
                                    rs.getLong("id"),
                                    rs.getLong("user_id"),
                                    rs.getTimestamp("created_at").toInstant(),
                                    CartStatus.valueOf(rs.getString("status"))
                            )
                    );
                }
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while searching cart by user id: " + e.getMessage(), e);
        }
        return carts;
    }
//    save cart
    @Override
    public Cart save(Cart cart) {
        String sql = """
                INSERT INTO carts(
                user_id,
                created_at,
                status
                ) VALUES (?, ?, ?)
                RETURNING id
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cart.getUserId());
            ps.setTimestamp(2, java.sql.Timestamp.from(cart.getCreatedAt()));
            ps.setString(3,cart.getCartStatus().name());
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    throw new SQLException("Obtaining cart failed");
                }
                return new Cart(
                        rs.getLong("id"),
                        cart.getUserId(),
                        cart.getCreatedAt(),
                        cart.getCartStatus()
                );
            }
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while saving cart to database: " + e.getMessage(), e);
        }
    }
//    delete cart - // hard delete
    @Override
    public void deleteById(Long cartId) {
        String sql = """
                DELETE FROM carts WHERE id = ?;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setLong(1, cartId);
            ps.executeUpdate();
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while deleting cart from database: " + e.getMessage(), e);
        }
    }
//    update cart - tạm thời chỉ status
    @Override
    public void update(Cart cart) {
        String sql = """
                UPDATE carts
                SET
                status = ?
                WHERE id = ?;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1,cart.getCartStatus().name());
            ps.setLong(2,cart.getCartId());
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while updating cart from database: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markCheckedOutIfActive(Long cartId) {
        String sql = """
                UPDATE carts
                SET status = 'CHECKED_OUT'
                WHERE id = ? AND status = 'ACTIVE';
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            // PostgreSQL locks the matching cart row. A concurrent checkout waits,
            // then re-evaluates status after the first transaction commits/rolls back.
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error while claiming cart for checkout: " + e.getMessage(), e);
        }
    }
}
