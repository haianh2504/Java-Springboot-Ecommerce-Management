package product.service;

import exception.business.detailed_exceptions.InsufficientStockException;
import exception.business.detailed_exceptions.ProductNameAlreadyInUseException;
import exception.resource.detailed_exceptions.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.entities.*;
import product.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductManagementServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductManagementServiceImpl productManagementServiceImpl;

    // helper functions
    private Product createPersistedPhysicalProduct()
    {
        return new PhysicalProduct(
                1L,
                new ProductName("Mechanical Keyboard"),
                10,
                new BigDecimal("89.99"),
                ProductStatus.INACTIVE,
                ProductType.PHYSICAL,
                Instant.parse("2026-09-10T00:00:00Z"),
                new BigDecimal("1.25")
        );
    }

    private Product createPersistedDigitalProduct()
    {
        return new DigitalProduct(
                2L,
                new ProductName("Java E-Book"),
                100,
                new BigDecimal("29.99"),
                ProductStatus.INACTIVE,
                ProductType.DIGITAL,
                Instant.parse("2026-09-10T00:00:00Z")
        );
    }

    // Create Physical Product with valid arguments -> save and return Product
    @Test
    @DisplayName("Create a physical product with valid arguments, then save and return the persisted product")
    void createPhysicalProduct_validArguments_savesAndReturnsProduct()
    {
        // --GIVEN--
        ProductName name = new ProductName("Mechanical Keyboard");
        int stockQuantity = 10;
        BigDecimal basePrice = new BigDecimal("89.99");
        BigDecimal weight = new BigDecimal("1.25");
        Product savedProduct = createPersistedPhysicalProduct();
        when(productRepository.findByName(name)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // --WHEN--
        Product actualProduct = productManagementServiceImpl.createNewPhysicalProduct(
                name, stockQuantity, basePrice, weight
        );

        // --THEN--
        assertAll(
                () -> assertSame(savedProduct, actualProduct),
                () -> assertEquals(1L, actualProduct.getId()),
                () -> assertInstanceOf(PhysicalProduct.class, actualProduct),
                () -> assertSame(ProductStatus.INACTIVE, actualProduct.getStatus()),
                () -> assertSame(ProductType.PHYSICAL, actualProduct.getProductType())
        );
        verify(productRepository).findByName(name);
        verify(productRepository).save(argThat(product ->
                product instanceof PhysicalProduct physicalProduct
                        && product.getId() == null
                        && product.getName() == name
                        && product.getQuantity() == stockQuantity
                        && product.getBasePrice().compareTo(basePrice) == 0
                        && product.getStatus() == ProductStatus.INACTIVE
                        && product.getProductType() == ProductType.PHYSICAL
                        && physicalProduct.getWeight().compareTo(weight) == 0
        ));
    }

    // Create Physical product with existing name -> throw ProductNameAlreadyInUse
    @Test
    @DisplayName("Create a physical product with an existing name throws ProductNameAlreadyInUseException")
    void createPhysicalProduct_existingName_throwsProductNameAlreadyInUseException()
    {
        // --GIVEN--
        Product existingProduct = createPersistedPhysicalProduct();
        ProductName name = existingProduct.getName();
        when(productRepository.findByName(name)).thenReturn(Optional.of(existingProduct));

        // --WHEN--
        ProductNameAlreadyInUseException exception = assertThrows(
                ProductNameAlreadyInUseException.class,
                () -> productManagementServiceImpl.createNewPhysicalProduct(
                        name, 10, new BigDecimal("89.99"), new BigDecimal("1.25")
                )
        );

        // --THEN--
        assertEquals("This product name has already been used", exception.getMessage());
        verify(productRepository).findByName(name);
        verify(productRepository, never()).save(any(Product.class));
    }

    // Create Physical Product with negative stock quantity -> throw IllegalArgumentException
    @Test
    @DisplayName("Create a physical product with negative stock quantity throws IllegalArgumentException")
    void createPhysicalProduct_negativeStockQuantity_throwsIllegalArgumentException()
    {
        // --GIVEN--
        ProductName name = new ProductName("Mechanical Keyboard");
        int invalidStockQuantity = -1;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productManagementServiceImpl.createNewPhysicalProduct(
                        name, invalidStockQuantity, new BigDecimal("89.99"), new BigDecimal("1.25")
                )
        );

        // --THEN--
        assertEquals("Invalid stock quantity", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Create physical product with invalid base price -> throw Illegal argument exception
    @Test
    @DisplayName("Create a physical product with a non-positive base price throws IllegalArgumentException")
    void createPhysicalProduct_nonPositiveBasePrice_throwsIllegalArgumentException()
    {
        // --GIVEN--
        ProductName name = new ProductName("Mechanical Keyboard");
        BigDecimal invalidBasePrice = BigDecimal.ZERO;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productManagementServiceImpl.createNewPhysicalProduct(
                        name, 10, invalidBasePrice, new BigDecimal("1.25")
                )
        );

        // --THEN--
        assertEquals("Product base price has to be bigger than Zero", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Create physical product with null arguments ( using ParameterizedTest and Stream )
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullPhysicalProductArguments")
    @DisplayName("Create a physical product with a null required argument throws NullPointerException")
    void createPhysicalProduct_nullRequiredArgument_throwsNullPointerException(
            String nullArgument, ProductName name, BigDecimal basePrice,
            BigDecimal weight, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullPhysicalProductArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.createNewPhysicalProduct(name, 10, basePrice, weight)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    static Stream<Arguments> nullPhysicalProductArguments()
    {
        ProductName name = new ProductName("Mechanical Keyboard");
        BigDecimal basePrice = new BigDecimal("89.99");
        BigDecimal weight = new BigDecimal("1.25");

        return Stream.of(
                Arguments.of("name", null, basePrice, weight, "Product name cannot be null"),
                Arguments.of("base price", name, null, weight, "Product base price cannot be null"),
                Arguments.of("weight", name, basePrice, null, "Product weight cannot be null")
        );
    }

    // Create physical product with invalid weight -> throw exception
    @Test
    @DisplayName("Create a physical product with a non-positive weight throws IllegalArgumentException")
    void createPhysicalProduct_nonPositiveWeight_throwsIllegalArgumentException()
    {
        // --GIVEN--
        ProductName name = new ProductName("Mechanical Keyboard");
        BigDecimal invalidWeight = BigDecimal.ZERO;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productManagementServiceImpl.createNewPhysicalProduct(
                        name, 10, new BigDecimal("89.99"), invalidWeight
                )
        );

        // --THEN--
        assertEquals("Product weight has to be bigger than Zero", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Create digital Product with valid arguments -> save and return Product
    @Test
    @DisplayName("Create a digital product with valid arguments, then save and return the persisted product")
    void createDigitalProduct_validArguments_savesAndReturnsProduct()
    {
        // --GIVEN--
        ProductName name = new ProductName("Java E-Book");
        int stockQuantity = 100;
        BigDecimal basePrice = new BigDecimal("29.99");
        Product savedProduct = createPersistedDigitalProduct();
        when(productRepository.findByName(name)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // --WHEN--
        Product actualProduct = productManagementServiceImpl.createNewDigitalProduct(
                name, stockQuantity, basePrice
        );

        // --THEN--
        assertAll(
                () -> assertSame(savedProduct, actualProduct),
                () -> assertEquals(2L, actualProduct.getId()),
                () -> assertInstanceOf(DigitalProduct.class, actualProduct),
                () -> assertSame(ProductStatus.INACTIVE, actualProduct.getStatus()),
                () -> assertSame(ProductType.DIGITAL, actualProduct.getProductType())
        );
        verify(productRepository).findByName(name);
        verify(productRepository).save(argThat(product ->
                product instanceof DigitalProduct
                        && product.getId() == null
                        && product.getName() == name
                        && product.getQuantity() == stockQuantity
                        && product.getBasePrice().compareTo(basePrice) == 0
                        && product.getStatus() == ProductStatus.INACTIVE
                        && product.getProductType() == ProductType.DIGITAL
        ));
    }

    // Create digital product with existing name -> throw ProductNameAlreadyInUse
    @Test
    @DisplayName("Create a digital product with an existing name throws ProductNameAlreadyInUseException")
    void createDigitalProduct_existingName_throwsProductNameAlreadyInUseException()
    {
        // --GIVEN--
        Product existingProduct = createPersistedDigitalProduct();
        ProductName name = existingProduct.getName();
        when(productRepository.findByName(name)).thenReturn(Optional.of(existingProduct));

        // --WHEN--
        ProductNameAlreadyInUseException exception = assertThrows(
                ProductNameAlreadyInUseException.class,
                () -> productManagementServiceImpl.createNewDigitalProduct(
                        name, 100, new BigDecimal("29.99")
                )
        );

        // --THEN--
        assertEquals("This product name has already been used", exception.getMessage());
        verify(productRepository).findByName(name);
        verify(productRepository, never()).save(any(Product.class));
    }

    // Create digital Product with negative stock quantity -> throw IllegalArgumentException
    @Test
    @DisplayName("Create a digital product with negative stock quantity throws IllegalArgumentException")
    void createDigitalProduct_negativeStockQuantity_throwsIllegalArgumentException()
    {
        // --GIVEN--
        ProductName name = new ProductName("Java E-Book");
        int invalidStockQuantity = -1;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productManagementServiceImpl.createNewDigitalProduct(
                        name, invalidStockQuantity, new BigDecimal("29.99")
                )
        );

        // --THEN--
        assertEquals("Product quantity cannot be negative", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Create digital product with invalid base price -> throw Illegal argument exception
    @Test
    @DisplayName("Create a digital product with a non-positive base price throws IllegalArgumentException")
    void createDigitalProduct_nonPositiveBasePrice_throwsIllegalArgumentException()
    {
        // --GIVEN--
        ProductName name = new ProductName("Java E-Book");
        BigDecimal invalidBasePrice = BigDecimal.ZERO;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productManagementServiceImpl.createNewDigitalProduct(name, 100, invalidBasePrice)
        );

        // --THEN--
        assertEquals("Product base price has to be bigger than Zero", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Create digital product with null arguments ( using ParameterizedTest and Stream )
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullDigitalProductArguments")
    @DisplayName("Create a digital product with a null required argument throws NullPointerException")
    void createDigitalProduct_nullRequiredArgument_throwsNullPointerException(
            String nullArgument, ProductName name, BigDecimal basePrice, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullDigitalProductArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.createNewDigitalProduct(name, 100, basePrice)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    static Stream<Arguments> nullDigitalProductArguments()
    {
        ProductName name = new ProductName("Java E-Book");
        BigDecimal basePrice = new BigDecimal("29.99");

        return Stream.of(
                Arguments.of("name", null, basePrice, "Product name cannot be null"),
                Arguments.of("base price", name, null, "Product base price cannot be null")
        );
    }

    // Find product with valid Id provided -> return product
    @Test
    @DisplayName("Find a product with a valid ID returns the persisted product")
    void findProductById_validId_returnsPersistedProduct()
    {
        // --GIVEN--
        Long productId = 1L;
        Product persistedProduct = createPersistedPhysicalProduct();
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        Product actualProduct = productManagementServiceImpl.findProductById(productId);

        // --THEN--
        assertSame(persistedProduct, actualProduct);
        verify(productRepository).findById(productId);
    }

    // Find product but productId not found -> return ProductNotFoundException
    @Test
    @DisplayName("Find a product with an unknown ID throws ProductNotFoundException")
    void findProductById_unknownId_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.findProductById(productId)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
    }

    // Find product but Id is null -> return null pointer exception
    @Test
    @DisplayName("Find a product with a null ID throws NullPointerException")
    void findProductById_nullId_throwsNullPointerException()
    {
        // --GIVEN--
        Long productId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.findProductById(productId)
        );

        // --THEN--
        assertEquals("Product id cannot be null", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Find product with valid name provided and return persisted product
    @Test
    @DisplayName("Find a product with a valid name returns the persisted product")
    void findProductByName_validName_returnsPersistedProduct()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        ProductName productName = persistedProduct.getName();
        when(productRepository.findByName(productName)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        Product actualProduct = productManagementServiceImpl.findProductByName(productName);

        // --THEN--
        assertSame(persistedProduct, actualProduct);
        verify(productRepository).findByName(productName);
    }

    // Find product with valid name provided but not found, throw ProductNotFoundException
    @Test
    @DisplayName("Find a product with an unknown name throws ProductNotFoundException")
    void findProductByName_unknownName_throwsProductNotFoundException()
    {
        // --GIVEN--
        ProductName productName = new ProductName("Unknown Product");
        when(productRepository.findByName(productName)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.findProductByName(productName)
        );

        // --THEN--
        assertEquals("Product with name [" + productName + "] not found", exception.getMessage());
        verify(productRepository).findByName(productName);
    }

    // Find product with null name, throw NullPointerException
    @Test
    @DisplayName("Find a product with a null name throws NullPointerException")
    void findProductByName_nullName_throwsNullPointerException()
    {
        // --GIVEN--
        ProductName productName = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.findProductByName(productName)
        );

        // --THEN--
        assertEquals("Product name cannot be null", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Update product with valid name successfully
    @Test
    @DisplayName("Update an existing product with a valid new name successfully")
    void updateProductName_validArguments_updatesProduct()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        ProductName newName = new ProductName("Wireless Mechanical Keyboard");
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.updateProductName(productId, newName);

        // --THEN--
        assertSame(newName, persistedProduct.getName());
        verify(productRepository).findById(productId);
        verify(productRepository).update(persistedProduct);
    }

    // Update product name but product not found, throw ProductNotFoundException
    @Test
    @DisplayName("Update the name of an unknown product throws ProductNotFoundException")
    void updateProductName_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        ProductName newName = new ProductName("Wireless Mechanical Keyboard");
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.updateProductName(productId, newName)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Update product but null arguments, throw NullPointerException ( use ParameterizedTest and Stream )
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullUpdateProductNameArguments")
    @DisplayName("Update a product name with a null required argument throws NullPointerException")
    void updateProductName_nullRequiredArgument_throwsNullPointerException(
            String nullArgument, Long productId, ProductName newName, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullUpdateProductNameArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.updateProductName(productId, newName)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    static Stream<Arguments> nullUpdateProductNameArguments()
    {
        return Stream.of(
                Arguments.of("product ID", null, new ProductName("New Product Name"),
                        "ProductId cannot be null"),
                Arguments.of("new name", 1L, null, "Product name cannot be null")
        );
    }

    // Update product weight with valid weight successfully
    @Test
    @DisplayName("Update an existing physical product with a valid new weight successfully")
    void updateProductWeight_validArguments_updatesPhysicalProduct()
    {
        // --GIVEN--
        PhysicalProduct persistedProduct = (PhysicalProduct) createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        BigDecimal newWeight = new BigDecimal("2.50");
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.updateProductWeight(productId, newWeight);

        // --THEN--
        assertEquals(0, newWeight.compareTo(persistedProduct.getWeight()));
        verify(productRepository).findById(productId);
        verify(productRepository).update(persistedProduct);
    }

    // Update product weight but product not found, throw ProductNotFoundException
    @Test
    @DisplayName("Update the weight of an unknown product throws ProductNotFoundException")
    void updateProductWeight_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        BigDecimal newWeight = new BigDecimal("2.50");
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.updateProductWeight(productId, newWeight)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Update product weight but for wrong type of product, throw IllegalStateException
    @Test
    @DisplayName("Update the weight of a digital product throws IllegalStateException")
    void updateProductWeight_digitalProduct_throwsIllegalStateException()
    {
        // --GIVEN--
        Product digitalProduct = createPersistedDigitalProduct();
        Long productId = digitalProduct.getId();
        BigDecimal newWeight = new BigDecimal("2.50");
        when(productRepository.findById(productId)).thenReturn(Optional.of(digitalProduct));

        // --WHEN--
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productManagementServiceImpl.updateProductWeight(productId, newWeight)
        );

        // --THEN--
        assertEquals("Digital Product do not have weight", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Update product weight with same value, update not working
    @Test
    @DisplayName("Update a physical product with the same weight does not call repository update")
    void updateProductWeight_sameWeight_doesNotUpdateProduct()
    {
        // --GIVEN--
        PhysicalProduct persistedProduct = (PhysicalProduct) createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        BigDecimal sameWeight = new BigDecimal("1.250");
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.updateProductWeight(productId, sameWeight);

        // --THEN--
        assertEquals(0, sameWeight.compareTo(persistedProduct.getWeight()));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Update product weight with null arguments, throw NullPointerException ( using ParameterizedTest and Stream )
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullUpdateProductWeightArguments")
    @DisplayName("Update a product weight with a null required argument throws NullPointerException")
    void updateProductWeight_nullRequiredArgument_throwsNullPointerException(
            String nullArgument, Long productId, BigDecimal newWeight, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullUpdateProductWeightArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.updateProductWeight(productId, newWeight)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    static Stream<Arguments> nullUpdateProductWeightArguments()
    {
        return Stream.of(
                Arguments.of("product ID", null, new BigDecimal("2.50"), "ProductId cannot be null"),
                Arguments.of("new weight", 1L, null, "Product new weight cannot be null")
        );
    }

    // Update product base price with valid arguments successfully
    @Test
    @DisplayName("Update an existing product with a valid new base price successfully")
    void updateBasePrice_validArguments_updatesProduct()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        BigDecimal newBasePrice = new BigDecimal("99.99");
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.updateBasePrice(productId, newBasePrice);

        // --THEN--
        assertEquals(0, newBasePrice.compareTo(persistedProduct.getBasePrice()));
        verify(productRepository).findById(productId);
        verify(productRepository).update(persistedProduct);
    }

    // Update product base price but product not found, throw ProductNotFoundException
    @Test
    @DisplayName("Update the base price of an unknown product throws ProductNotFoundException")
    void updateBasePrice_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        BigDecimal newBasePrice = new BigDecimal("99.99");
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.updateBasePrice(productId, newBasePrice)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Update product base price with same value, update not working
    @Test
    @DisplayName("Update a product with the same base price does not call repository update")
    void updateBasePrice_sameValue_doesNotUpdateProduct()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        BigDecimal sameBasePrice = new BigDecimal("89.990");
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.updateBasePrice(productId, sameBasePrice);

        // --THEN--
        assertEquals(0, sameBasePrice.compareTo(persistedProduct.getBasePrice()));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Update product base price with null arguments, throw NullPointerException ( using ParameterizedTest and Stream )
    @ParameterizedTest(name = "{index}: null {0}")
    @MethodSource("nullUpdateBasePriceArguments")
    @DisplayName("Update a product base price with a null required argument throws NullPointerException")
    void updateBasePrice_nullRequiredArgument_throwsNullPointerException(
            String nullArgument, Long productId, BigDecimal newBasePrice, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullUpdateBasePriceArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.updateBasePrice(productId, newBasePrice)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    static Stream<Arguments> nullUpdateBasePriceArguments()
    {
        return Stream.of(
                Arguments.of("product ID", null, new BigDecimal("99.99"), "ProductId cannot be null"),
                Arguments.of("new base price", 1L, null, "Product baseprice cannot be null")
        );
    }

    // Decrease quantity with valid arguments successfully
    @Test
    @DisplayName("Decrease the stock quantity of an existing product successfully")
    void decreaseStockQuantity_validArguments_decreasesQuantitySuccessfully()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        int decreaseQuantity = 3;
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));
        when(productRepository.decreaseQuantity(productId, decreaseQuantity)).thenReturn(true);

        // --WHEN--
        productManagementServiceImpl.decreaseStockQuantity(productId, decreaseQuantity);

        // --THEN--
        verify(productRepository).findById(productId);
        verify(productRepository).decreaseQuantity(productId, decreaseQuantity);
    }

    // Decrease quantity with new invalid argument, throw IllegalArgumentException
    @Test
    @DisplayName("Decrease stock quantity by a non-positive number throws IllegalArgumentException")
    void decreaseStockQuantity_nonPositiveQuantity_throwsIllegalArgumentException()
    {
        // --GIVEN--
        Long productId = 1L;
        int invalidDecreaseQuantity = 0;

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productManagementServiceImpl.decreaseStockQuantity(
                        productId, invalidDecreaseQuantity
                )
        );

        // --THEN--
        assertEquals("Product quantity cannot be negative", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Decrease quantity but no product found
    @Test
    @DisplayName("Decrease the stock quantity of an unknown product throws ProductNotFoundException")
    void decreaseStockQuantity_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        int decreaseQuantity = 3;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.decreaseStockQuantity(productId, decreaseQuantity)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).decreaseQuantity(anyLong(), anyInt());
    }

    // Decrease invalid number (excessive), throw InsufficientStockException
    @Test
    @DisplayName("Decrease stock by more than the available quantity throws InsufficientStockException")
    void decreaseStockQuantity_insufficientStock_throwsInsufficientStockException()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        int excessiveDecreaseQuantity = persistedProduct.getQuantity() + 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));
        when(productRepository.decreaseQuantity(productId, excessiveDecreaseQuantity)).thenReturn(false);

        // --WHEN--
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> productManagementServiceImpl.decreaseStockQuantity(
                        productId, excessiveDecreaseQuantity
                )
        );

        // --THEN--
        assertEquals(
                "Product [id=1] does not have enough stock: requested 11, available 10",
                exception.getMessage()
        );
        verify(productRepository).findById(productId);
        verify(productRepository).decreaseQuantity(productId, excessiveDecreaseQuantity);
    }

    // Activate product successfully
    @Test
    @DisplayName("Activate an existing product successfully")
    void activateProduct_existingProduct_activatesAndUpdatesProduct()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        Long productId = persistedProduct.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.activateProduct(productId);

        // --THEN--
        assertSame(ProductStatus.ACTIVE, persistedProduct.getStatus());
        verify(productRepository).findById(productId);
        verify(productRepository).update(persistedProduct);
    }

    // Activate a not found product, throw ProductNotFoundException
    @Test
    @DisplayName("Activate an unknown product throws ProductNotFoundException")
    void activateProduct_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.activateProduct(productId)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Activate product but null argument provided, throw NullPointerException
    @Test
    @DisplayName("Activate a product with a null ID throws NullPointerException")
    void activateProduct_nullId_throwsNullPointerException()
    {
        // --GIVEN--
        Long productId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.activateProduct(productId)
        );

        // --THEN--
        assertEquals("Product id cannot be null", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

    // Deactivate product successfully
    @Test
    @DisplayName("Deactivate an existing active product successfully")
    void deactivateProduct_existingActiveProduct_deactivatesAndUpdatesProduct()
    {
        // --GIVEN--
        Product persistedProduct = createPersistedPhysicalProduct();
        persistedProduct.activate();
        Long productId = persistedProduct.getId();
        when(productRepository.findById(productId)).thenReturn(Optional.of(persistedProduct));

        // --WHEN--
        productManagementServiceImpl.deactivateProduct(productId);

        // --THEN--
        assertSame(ProductStatus.INACTIVE, persistedProduct.getStatus());
        verify(productRepository).findById(productId);
        verify(productRepository).update(persistedProduct);
    }

    // Deactivate a not found product, throw ProductNotFoundException
    @Test
    @DisplayName("Deactivate an unknown product throws ProductNotFoundException")
    void deactivateProduct_unknownProduct_throwsProductNotFoundException()
    {
        // --GIVEN--
        Long productId = 99L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // --WHEN--
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productManagementServiceImpl.deactivateProduct(productId)
        );

        // --THEN--
        assertEquals("Product with id 99 not found", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    // Deactivate a product but null argument provided, throw NullPointerException
    @Test
    @DisplayName("Deactivate a product with a null ID throws NullPointerException")
    void deactivateProduct_nullId_throwsNullPointerException()
    {
        // --GIVEN--
        Long productId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> productManagementServiceImpl.deactivateProduct(productId)
        );

        // --THEN--
        assertEquals("Product id cannot be null", exception.getMessage());
        verifyNoInteractions(productRepository);
    }

}
