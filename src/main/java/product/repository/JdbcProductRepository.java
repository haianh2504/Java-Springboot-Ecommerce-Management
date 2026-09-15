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
                type,
                created_at,
                weight
                )
                VALUES(?,?,?,?,?,?,?)
                RETURNING id
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, product.getName().toString());
            ps.setInt(2, product.getQuantity());
            ps.setBigDecimal(3,product.getBasePrice());
            ps.setString(4,product.getStatus().toString());
            ps.setString(5,product.getProductType().toString());
            ps.setTimestamp(6,java.sql.Timestamp.from(product.getCreatedAt()));
            // thuộc tính riêng của physical product
            if(product instanceof PhysicalProduct)
            {
                ps.setBigDecimal(7,((PhysicalProduct) product).getWeight());
                // Thực thi câu lệnh INSERT xuống Postgre
                try(ResultSet rs = ps.executeQuery())
                {
                    if(!rs.next()){
                        throw new SQLException("Saving and return product failed");
                    }
                    return new PhysicalProduct(
                            rs.getLong("id"),
                            product.getName(),
                            product.getQuantity(),
                            product.getBasePrice(),
                            product.getStatus(),
                            product.getProductType(),
                            product.getCreatedAt(),
                            ((PhysicalProduct) product).getWeight()
                    );
                }
            }
            // if being digital product
            ps.setNull(7, Types.DECIMAL);
            // Thực thi câu lệnh INSERT xuống Postgre
            try(ResultSet rs = ps.executeQuery())
            {
                if(!rs.next()){
                    throw new SQLException("Saving and return product failed");
                }
                return new DigitalProduct(
                        rs.getLong("id"),
                        product.getName(),
                        product.getQuantity(),
                        product.getBasePrice(),
                        product.getStatus(),
                        product.getProductType(),
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
                type,
                created_at,
                weight
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
                ProductType type = ProductType.valueOf(rs.getString("type"));
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                BigDecimal weight = rs.getBigDecimal("weight");
                Product data;
                if(weight != null)
                {
                    data = new PhysicalProduct(
                            productId,
                            name,
                            quantity,
                            price,
                            status,
                            type,
                            created_at,
                            weight
                    );
                }
                else{
                    data = new DigitalProduct(
                            productId,
                            name,
                            quantity,
                            price,
                            status,
                            type,
                            created_at
                    );
                }
                return Optional.of(data);
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
                type,
                weight,
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
                ProductType type = ProductType.valueOf(rs.getString("type"));
                Instant created_at = rs.getTimestamp("created_at").toInstant();
                ProductStatus status = ProductStatus.valueOf(rs.getString("status"));
                BigDecimal weight = rs.getBigDecimal("weight");
                if(type == ProductType.PHYSICAL)
                {
                    return Optional.of(new PhysicalProduct(
                            id,
                            name,
                            quantity,
                            price,
                            status,
                            type,
                            created_at,
                            weight
                    ));
                }
                else{
                    return Optional.of(new DigitalProduct(
                            id,
                            name,
                            quantity,
                            price,
                            status,
                            type,
                            created_at
                    ));
                }
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
                status = ?,
                weight = ?
                WHERE id = ?;
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, product.getName().toString());
            ps.setInt(2,product.getQuantity());
            ps.setBigDecimal(3, product.getBasePrice());
            ps.setString(4, product.getStatus().toString());
            if(product instanceof PhysicalProduct physicalProduct)
            {
                ps.setBigDecimal(5,physicalProduct.getWeight());
            }
            else{
                ps.setNull(5, Types.DECIMAL);
            }
            ps.setLong(6, product.getId());
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
