package order_item.service;

import exception.business.detailed_exceptions.OrderItemAlreadyExistsException;
import exception.resource.detailed_exceptions.OrderItemNotFoundException;
import exception.resource.detailed_exceptions.OrderNotFoundException;
import order.entities.Order;
import order.repository.OrderRepository;
import order_item.entities.OrderItem;
import order_item.repository.OrderItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemManagementServiceImplTest {
    private static final Long ORDER_ID = 10L;
    private static final Long PRODUCT_ID = 20L;
    private static final int QUANTITY = 2;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("15.50");

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<OrderItem> orderItemCaptor;

    @InjectMocks
    private OrderItemManagementServiceImpl orderItemManagementServiceImpl;

    private OrderItem createPersistedOrderItem()
    {
        return new OrderItem(1L, ORDER_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE);
    }

    private void stubExistingOrder()
    {
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(mock(Order.class)));
    }

    @Test
    @DisplayName("Constructing the service with a null order-item repository throws NullPointerException")
    void constructor_nullOrderItemRepository_throwsNullPointerException()
    {
        // --GIVEN--
        OrderItemRepository nullOrderItemRepository = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrderItemManagementServiceImpl(nullOrderItemRepository, orderRepository)
        );

        // --THEN--
        assertEquals("orderItemRepo must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Constructing the service with a null order repository throws NullPointerException")
    void constructor_nullOrderRepository_throwsNullPointerException()
    {
        // --GIVEN--
        OrderRepository nullOrderRepository = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrderItemManagementServiceImpl(orderItemRepository, nullOrderRepository)
        );

        // --THEN--
        assertEquals("orderRepository must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Creating an order item with valid values saves and returns the submitted item")
    void createNewOrderItem_validArguments_savesAndReturnsOrderItem()
    {
        // --GIVEN--
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // --WHEN--
        OrderItem actualOrderItem = orderItemManagementServiceImpl.createNewOrderItem(
                ORDER_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE
        );

        // --THEN--
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
        verify(orderItemRepository).save(orderItemCaptor.capture());
        OrderItem capturedOrderItem = orderItemCaptor.getValue();
        assertAll(
                () -> assertSame(capturedOrderItem, actualOrderItem),
                () -> assertNull(capturedOrderItem.getOrderItemId()),
                () -> assertEquals(ORDER_ID, capturedOrderItem.getOrderId()),
                () -> assertEquals(PRODUCT_ID, capturedOrderItem.getProductId()),
                () -> assertEquals(QUANTITY, capturedOrderItem.getQuantity()),
                () -> assertEquals(0, capturedOrderItem.getUnitPrice().compareTo(UNIT_PRICE))
        );
    }

    @Test
    @DisplayName("Creating an order item for an unknown order throws OrderNotFoundException")
    void createNewOrderItem_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderItemManagementServiceImpl.createNewOrderItem(
                        ORDER_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE
                )
        );

        // --THEN--
        assertEquals("Order with id 10 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verifyNoInteractions(orderItemRepository);
    }

    @ParameterizedTest(name = "quantity = {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("Creating an order item with a non-positive quantity throws IllegalArgumentException")
    void createNewOrderItem_nonPositiveQuantity_throwsIllegalArgumentException(int invalidQuantity)
    {
        // --GIVEN--
        String expectedMessage = "quantity must be greater than 0";

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderItemManagementServiceImpl.createNewOrderItem(
                        ORDER_ID, PRODUCT_ID, invalidQuantity, UNIT_PRICE
                )
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }

    @ParameterizedTest(name = "unit price = {0}")
    @ValueSource(strings = {"0.00", "-0.01"})
    @DisplayName("Creating an order item with a non-positive unit price throws IllegalArgumentException")
    void createNewOrderItem_nonPositiveUnitPrice_throwsIllegalArgumentException(String unitPriceValue)
    {
        // --GIVEN--
        BigDecimal invalidUnitPrice = new BigDecimal(unitPriceValue);

        // --WHEN--
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderItemManagementServiceImpl.createNewOrderItem(
                        ORDER_ID, PRODUCT_ID, QUANTITY, invalidUnitPrice
                )
        );

        // --THEN--
        assertEquals("unit_price must be greater than 0", exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }

    @ParameterizedTest(name = "null {0}")
    @MethodSource("nullCreateArguments")
    @DisplayName("Creating an order item with a null required argument throws NullPointerException")
    void createNewOrderItem_nullRequiredArgument_throwsNullPointerException(
            String nullArgument, Long orderId, Long productId,
            BigDecimal unitPrice, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullCreateArguments().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderItemManagementServiceImpl.createNewOrderItem(
                        orderId, productId, QUANTITY, unitPrice
                )
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }

    static Stream<Arguments> nullCreateArguments()
    {
        return Stream.of(
                Arguments.of("order ID", null, PRODUCT_ID, UNIT_PRICE, "orderId must not be null"),
                Arguments.of("product ID", ORDER_ID, null, UNIT_PRICE, "productId must not be null"),
                Arguments.of("unit price", ORDER_ID, PRODUCT_ID, null, "unit_price must not be null")
        );
    }

    @Test
    @DisplayName("Creating an order item that already exists throws OrderItemAlreadyExistsException")
    void createNewOrderItem_existingOrderItem_throwsOrderItemAlreadyExistsException()
    {
        // --GIVEN--
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(createPersistedOrderItem()));

        // --WHEN--
        OrderItemAlreadyExistsException exception = assertThrows(
                OrderItemAlreadyExistsException.class,
                () -> orderItemManagementServiceImpl.createNewOrderItem(
                        ORDER_ID, PRODUCT_ID, QUANTITY, UNIT_PRICE
                )
        );

        // --THEN--
        assertEquals(
                "This order item has already existed in orderId: 10with the productId: 20",
                exception.getMessage()
        );
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("Deleting an existing order item removes it from its order")
    void deleteOrderItem_existingOrderItem_deletesSuccessfully()
    {
        // --GIVEN--
        OrderItem persistedOrderItem = createPersistedOrderItem();
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(persistedOrderItem));

        // --WHEN--
        orderItemManagementServiceImpl.deleteOrderItem(ORDER_ID, PRODUCT_ID);

        // --THEN--
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
        verify(orderItemRepository).delete(ORDER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Deleting an order item from an unknown order throws OrderNotFoundException")
    void deleteOrderItem_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderItemManagementServiceImpl.deleteOrderItem(ORDER_ID, PRODUCT_ID)
        );

        // --THEN--
        assertEquals("Order with id 10 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("Deleting an unknown order item throws OrderItemNotFoundException")
    void deleteOrderItem_unknownOrderItem_throwsOrderItemNotFoundException()
    {
        // --GIVEN--
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // --WHEN--
        OrderItemNotFoundException exception = assertThrows(
                OrderItemNotFoundException.class,
                () -> orderItemManagementServiceImpl.deleteOrderItem(ORDER_ID, PRODUCT_ID)
        );

        // --THEN--
        assertEquals(
                "Order item with orderId 10 and productId 20 not found",
                exception.getMessage()
        );
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
        verify(orderItemRepository, never()).delete(anyLong(), anyLong());
    }

    @ParameterizedTest(name = "null {0}")
    @MethodSource("nullOrderAndProductIds")
    @DisplayName("Deleting an order item with a null required ID throws NullPointerException")
    void deleteOrderItem_nullRequiredId_throwsNullPointerException(
            String nullArgument, Long orderId, Long productId, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullOrderAndProductIds().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderItemManagementServiceImpl.deleteOrderItem(orderId, productId)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }

    @Test
    @DisplayName("Getting an existing order item returns the persisted item")
    void getOrderItemById_existingOrderItem_returnsPersistedOrderItem()
    {
        // --GIVEN--
        OrderItem persistedOrderItem = createPersistedOrderItem();
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(persistedOrderItem));

        // --WHEN--
        OrderItem actualOrderItem = orderItemManagementServiceImpl.getOrderItemById(
                ORDER_ID, PRODUCT_ID
        );

        // --THEN--
        assertSame(persistedOrderItem, actualOrderItem);
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Getting an order item from an unknown order throws OrderNotFoundException")
    void getOrderItemById_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderItemManagementServiceImpl.getOrderItemById(ORDER_ID, PRODUCT_ID)
        );

        // --THEN--
        assertEquals("Order with id 10 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("Getting an unknown order item throws OrderItemNotFoundException")
    void getOrderItemById_unknownOrderItem_throwsOrderItemNotFoundException()
    {
        // --GIVEN--
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // --WHEN--
        OrderItemNotFoundException exception = assertThrows(
                OrderItemNotFoundException.class,
                () -> orderItemManagementServiceImpl.getOrderItemById(ORDER_ID, PRODUCT_ID)
        );

        // --THEN--
        assertEquals(
                "Order item with orderId 10 and productId 20 not found",
                exception.getMessage()
        );
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
    }

    @ParameterizedTest(name = "null {0}")
    @MethodSource("nullOrderAndProductIds")
    @DisplayName("Getting an order item with a null required ID throws NullPointerException")
    void getOrderItemById_nullRequiredId_throwsNullPointerException(
            String nullArgument, Long orderId, Long productId, String expectedMessage)
    {
        // --GIVEN--
        // Arguments are supplied by nullOrderAndProductIds().

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderItemManagementServiceImpl.getOrderItemById(orderId, productId)
        );

        // --THEN--
        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }

    static Stream<Arguments> nullOrderAndProductIds()
    {
        return Stream.of(
                Arguments.of("order ID", null, PRODUCT_ID, "orderId must not be null"),
                Arguments.of("product ID", ORDER_ID, null, "productId must not be null")
        );
    }

    @Test
    @DisplayName("Getting order items for an existing order returns an unmodifiable list")
    void getOrderItemsByOrderId_existingOrder_returnsUnmodifiableOrderItems()
    {
        // --GIVEN--
        List<OrderItem> persistedOrderItems = new ArrayList<>(List.of(createPersistedOrderItem()));
        stubExistingOrder();
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(persistedOrderItems);

        // --WHEN--
        List<OrderItem> actualOrderItems = orderItemManagementServiceImpl.getOrderItemsByOrderId(ORDER_ID);

        // --THEN--
        assertAll(
                () -> assertEquals(persistedOrderItems, actualOrderItems),
                () -> assertThrows(
                        UnsupportedOperationException.class,
                        () -> actualOrderItems.add(createPersistedOrderItem())
                )
        );
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("Getting order items for an existing empty order returns an empty list")
    void getOrderItemsByOrderId_existingEmptyOrder_returnsEmptyList()
    {
        // --GIVEN--
        stubExistingOrder();
        when(orderItemRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        // --WHEN--
        List<OrderItem> actualOrderItems = orderItemManagementServiceImpl.getOrderItemsByOrderId(ORDER_ID);

        // --THEN--
        assertTrue(actualOrderItems.isEmpty());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("Getting order items for an unknown order throws OrderNotFoundException")
    void getOrderItemsByOrderId_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderItemManagementServiceImpl.getOrderItemsByOrderId(ORDER_ID)
        );

        // --THEN--
        assertEquals("Order with id 10 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("Getting order items with a null order ID throws NullPointerException")
    void getOrderItemsByOrderId_nullOrderId_throwsNullPointerException()
    {
        // --GIVEN--
        Long orderId = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderItemManagementServiceImpl.getOrderItemsByOrderId(orderId)
        );

        // --THEN--
        assertEquals("orderId must not be null", exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }

    @Test
    @DisplayName("Calculating an existing order item's total multiplies unit price by quantity")
    void getTotalPrice_existingOrderItem_returnsCalculatedTotal()
    {
        // --GIVEN--
        OrderItem persistedOrderItem = createPersistedOrderItem();
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(persistedOrderItem));

        // --WHEN--
        BigDecimal actualTotal = orderItemManagementServiceImpl.getTotalPrice(persistedOrderItem);

        // --THEN--
        assertEquals(0, actualTotal.compareTo(new BigDecimal("31.00")));
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Calculating an order item's total for an unknown order throws OrderNotFoundException")
    void getTotalPrice_unknownOrder_throwsOrderNotFoundException()
    {
        // --GIVEN--
        OrderItem orderItem = createPersistedOrderItem();
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // --WHEN--
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderItemManagementServiceImpl.getTotalPrice(orderItem)
        );

        // --THEN--
        assertEquals("Order with id 10 not found", exception.getMessage());
        verify(orderRepository).findByOrderId(ORDER_ID);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("Calculating an unknown order item's total throws OrderItemNotFoundException")
    void getTotalPrice_unknownOrderItem_throwsOrderItemNotFoundException()
    {
        // --GIVEN--
        OrderItem orderItem = createPersistedOrderItem();
        stubExistingOrder();
        when(orderItemRepository.findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        // --WHEN--
        OrderItemNotFoundException exception = assertThrows(
                OrderItemNotFoundException.class,
                () -> orderItemManagementServiceImpl.getTotalPrice(orderItem)
        );

        // --THEN--
        assertEquals(
                "Order item with orderId 10 and productId 20 not found",
                exception.getMessage()
        );
        verify(orderRepository).findByOrderId(ORDER_ID);
        verify(orderItemRepository).findByOrderIdAndProductId(ORDER_ID, PRODUCT_ID);
    }

    @Test
    @DisplayName("Calculating total price with a null order item throws NullPointerException")
    void getTotalPrice_nullOrderItem_throwsNullPointerException()
    {
        // --GIVEN--
        OrderItem orderItem = null;

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> orderItemManagementServiceImpl.getTotalPrice(orderItem)
        );

        // --THEN--
        assertEquals("orderItem must not be null", exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository);
    }
}
