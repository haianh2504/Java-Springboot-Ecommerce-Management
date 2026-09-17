package product.service;

import exception.business.detailed_exceptions.InsufficientStockException;
import exception.business.detailed_exceptions.ProductNameAlreadyInUseException;
import exception.resource.detailed_exceptions.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.entities.Product;
import product.entities.ProductName;
import product.entities.ProductStatus;
import product.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductManagementServiceImplTest {
    private static final Long PRODUCT_ID = 1L;
    private static final ProductName NAME = new ProductName("Mechanical Keyboard");
    private static final BigDecimal PRICE = new BigDecimal("89.99");

    @Mock
    private ProductRepository productRepository;

    private ProductManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductManagementServiceImpl(productRepository);
    }

    private Product persistedProduct() {
        return new Product(
                PRODUCT_ID, NAME, 10, PRICE, ProductStatus.ACTIVE,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    @Test
    @DisplayName("Create a product with valid values")
    void createProduct_validValues_savesProduct() {
        Product saved = persistedProduct();
        when(productRepository.findByName(NAME)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product actual = service.createProduct(NAME, 10, PRICE);

        assertSame(saved, actual);
        verify(productRepository).save(argThat(product ->
                product.getId() == null
                        && product.getName().equals(NAME)
                        && product.getQuantity() == 10
                        && product.getBasePrice().compareTo(PRICE) == 0
                        && product.getStatus() == ProductStatus.INACTIVE
        ));
    }

    @Test
    @DisplayName("Reject a duplicate product name")
    void createProduct_duplicateName_throwsException() {
        when(productRepository.findByName(NAME)).thenReturn(Optional.of(persistedProduct()));

        assertThrows(ProductNameAlreadyInUseException.class,
                () -> service.createProduct(NAME, 10, PRICE));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reject invalid values when creating a product")
    void createProduct_invalidValues_throwsException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> service.createProduct(null, 10, PRICE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.createProduct(NAME, -1, PRICE)),
                () -> assertThrows(NullPointerException.class,
                        () -> service.createProduct(NAME, 10, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.createProduct(NAME, 10, BigDecimal.ZERO))
        );
    }

    @Test
    @DisplayName("Find products by id and name")
    void findProduct_existingProduct_returnsProduct() {
        Product product = persistedProduct();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.findByName(NAME)).thenReturn(Optional.of(product));

        assertAll(
                () -> assertSame(product, service.findProductById(PRODUCT_ID)),
                () -> assertSame(product, service.findProductByName(NAME))
        );
    }

    @Test
    @DisplayName("Throw when a product does not exist")
    void findProductById_unknownProduct_throwsException() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findProductById(PRODUCT_ID));
    }

    @Test
    @DisplayName("Update a product name")
    void updateProductName_validName_updatesProduct() {
        Product product = persistedProduct();
        ProductName newName = new ProductName("Wireless Keyboard");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        service.updateProductName(PRODUCT_ID, newName);

        assertEquals(newName, product.getName());
        verify(productRepository).update(product);
    }

    @Test
    @DisplayName("Update a product price")
    void updateBasePrice_validPrice_updatesProduct() {
        Product product = persistedProduct();
        BigDecimal newPrice = new BigDecimal("99.99");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        service.updateBasePrice(PRODUCT_ID, newPrice);

        assertEquals(newPrice, product.getBasePrice());
        verify(productRepository).update(product);
    }

    @Test
    @DisplayName("Do not persist an unchanged product price")
    void updateBasePrice_samePrice_doesNotUpdate() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(persistedProduct()));

        service.updateBasePrice(PRODUCT_ID, PRICE);

        verify(productRepository, never()).update(any());
    }

    @Test
    @DisplayName("Activate and deactivate a product")
    void changeProductStatus_validProduct_updatesProduct() {
        Product product = persistedProduct();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        service.deactivateProduct(PRODUCT_ID);
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        service.activateProduct(PRODUCT_ID);

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        verify(productRepository, times(2)).update(product);
    }

    @Test
    @DisplayName("Decrease available stock atomically")
    void decreaseStockQuantity_availableStock_decreasesStock() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(persistedProduct()));
        when(productRepository.decreaseQuantity(PRODUCT_ID, 2)).thenReturn(true);

        service.decreaseStockQuantity(PRODUCT_ID, 2);

        verify(productRepository).decreaseQuantity(PRODUCT_ID, 2);
    }

    @Test
    @DisplayName("Throw when stock cannot be decreased")
    void decreaseStockQuantity_insufficientStock_throwsException() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(persistedProduct()));
        when(productRepository.decreaseQuantity(PRODUCT_ID, 20)).thenReturn(false);

        assertThrows(InsufficientStockException.class,
                () -> service.decreaseStockQuantity(PRODUCT_ID, 20));
    }

    @Test
    @DisplayName("Increase stock for an existing product")
    void increaseStockQuantity_existingProduct_increasesStock() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(persistedProduct()));

        service.increaseStockQuantity(PRODUCT_ID, 5);

        verify(productRepository).increaseQuantity(PRODUCT_ID, 5);
    }
}
