package product.repository;

import product.entities.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class JdbcProductRepository implements ProductRepository {
    private final Connection connection;
//    constructor
    public JdbcProductRepository(Connection connection)
    {
        this.connection = Objects.requireNonNull(connection, "Connection cannot be null");

    }
//    save Product
    @Override
    public Product save(Product product)
    {
        String sql = """
                INSERT INTO products(
                name,
                quantity,
                price,
                status,
                created_at
                )
                VALUES(?,?,?,?,?)
                RETURNING id
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, product.getName().toString());
            ps.setInt(2, product.getQuantity());
            ps.setBigDecimal(3,product.getBasePrice());
            ps.setString(4,product.getStatus().toString());
            ps.setTimestamp(5,java.sql.Timestamp.from(product.getCreatedAt()));
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    throw new SQLException("Saving and return product failed");
                }
                return new Product(
                        rs.getLong("id"),
                        product.getName(),
                        product.getQuantity(),
                        product.getBasePrice(),
                        product.getStatus(),
                        product.getCreatedAt()
                );
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error while saving product into DATABASE: " + e.getMessage(),e);
        }
    }
//    find product by id
    @Override
    public Optional<Product> findById(Long productId)
    {
        String sql = """
                SELECT
                name,
                quantity,
                price,
                status,
                created_at
                FROM products
                WHERE id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            // gán id vào ?
            ps.setLong(1, productId);
            // chạy lệnh SELECT
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next())
                {
                    return Optional.empty();
                }
                ProductName name = new ProductName(rs.getString("name"));
                int quantity = rs.getInt("quantity");
                BigDecimal price = rs.getBigDecimal("price");
                ProductStatus status = ProductStatus.valueOf(rs.getString("status"));
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                return Optional.of(new Product(
                        productId,
                        name,
                        quantity,
                        price,
                        status,
                        created_at
                ));
            }
        }catch(SQLException e)
        {
            throw new RuntimeException("Error while searching for products: " + e.getMessage(), e);
        }
    }
//    find product by name
    @Override
    public Optional<Product> findByName(ProductName name)
    {
        String sql = """
                SELECT
                id,
                name,
                quantity,
                price,
                status,
                created_at
                FROM products WHERE name = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, name.toString());
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    return Optional.empty();
                }
                Long id = rs.getLong("id");
                int quantity = rs.getInt("quantity");
                BigDecimal price = rs.getBigDecimal("price");
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                ProductStatus status = ProductStatus.valueOf(rs.getString("status"));
                return Optional.of(new Product(
                        id,
                        name,
                        quantity,
                        price,
                        status,
                        created_at
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while searching for product: " + e.getMessage(),e);
        }
    }
//    update product after changes
    @Override
    public void update(Product product)
    {
        String sql = """
                UPDATE products
                SET
                name = ?,
                quantity = ?,
                price = ?,
                status = ?
                WHERE id = ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, product.getName().toString());
            ps.setInt(2,product.getQuantity());
            ps.setBigDecimal(3, product.getBasePrice());
            ps.setString(4, product.getStatus().toString());
            ps.setLong(5, product.getId());
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while updating product: " + e.getMessage(), e);
        }
    }
//    decrease quantity
    @Override
    public boolean decreaseQuantity(Long productId, int quantity) {
        String sql = """
                UPDATE products
                SET quantity = quantity - ?
                WHERE id = ?
                  AND status = 'ACTIVE'
                  AND quantity >= ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            int affectedRows = ps.executeUpdate();
            // The stock check and subtraction happen in one statement. PostgreSQL
            // re-checks the predicate after waiting for a concurrent row lock, which
            // prevents two buyers from both spending the same remaining inventory.
            return affectedRows == 1;
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while decreasing product's quantity: " + e.getMessage(),e);
        }
    }
//    increase quantity
    @Override
    public void increaseQuantity(Long productId, int quantity) {
        String sql = """
                UPDATE products
                SET quantity = quantity + ?
                WHERE id = ?;
        """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }catch (SQLException e)
        {
            throw new RuntimeException("Error while increasing product's quantity: " + e.getMessage(),e);
        }
    }
}
