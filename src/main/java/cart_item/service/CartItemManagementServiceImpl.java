package cart_item.service;

import cart_item.entities.CartItem;
import cart_item.repository.CartItemRepository;
import exception.business.detailed_exceptions.CartItemAlreadyExistsException;
import exception.business.detailed_exceptions.InsufficientStockException;
import exception.business.detailed_exceptions.ProductInactiveException;
import exception.resource.detailed_exceptions.CartItemNotFoundException;
import exception.resource.detailed_exceptions.ProductNotFoundException;
import product.entities.Product;
import product.entities.ProductStatus;
import product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class CartItemManagementServiceImpl implements  CartItemManagementService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
//    constructor
    public CartItemManagementServiceImpl(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = Objects.requireNonNull(cartItemRepository, "cartItemRepository cannot be null");
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository cannot be null");
    }
//    get cartItems by cartId
    @Override
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        Objects.requireNonNull(cartId, "cartId cannot be null");
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        return List.copyOf(cartItems);
    }
    //    add new cartItem in cart
    @Override
    public CartItem addNewCartItem(Long cartId, Long productId, int number) {
        // validation
        Objects.requireNonNull(cartId, "cartId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(productId)
        );
        if(number<=0){
            throw new IllegalArgumentException("number must be greater than 0");
        }
        else if(number > product.getQuantity()){
            throw new InsufficientStockException(productId,number,product.getQuantity());
        }
        // check for existence
        if(cartItemRepository.findByCartIdAndProductId(cartId, productId).isPresent()){
            throw new CartItemAlreadyExistsException(cartId,productId);
        }
        // save
        CartItem cartItem = cartItemRepository.save(
                new CartItem(cartId, productId, number)
        );
        return cartItem;
    }
//    delete cartItem in cart
    @Override
    public void deleteCartItem(Long cartItemId) {
        Objects.requireNonNull(cartItemId, "cartItemId cannot be null");
        if(cartItemRepository.findByCartItemId(cartItemId).isEmpty()){
            throw new CartItemNotFoundException(cartItemId);
        }
        cartItemRepository.deleteByCartItemId(cartItemId);
    }
//    get CartItem by cartId & productId in cart
    @Override
    public CartItem getCartItemByCartIdAndProductId(Long cartId,Long productId) {
        Objects.requireNonNull(cartId, "cartId cannot be null");
        Objects.requireNonNull(productId, "productId cannot be null");
        return cartItemRepository.findByCartIdAndProductId(cartId,productId).orElseThrow(
                () -> new CartItemNotFoundException(cartId,productId)
        );
    }
//    update Item quantity
    @Override
    public CartItem updateCartItemQuantity(Long cartItemId, int newQuantity) {
        Objects.requireNonNull(cartItemId, "cartItemId cannot be null");
        if(newQuantity<=0){
            throw new IllegalArgumentException("newQuantity must be greater than 0");
        }
        CartItem cartItem = cartItemRepository.findByCartItemId(cartItemId).orElseThrow(
                () -> new CartItemNotFoundException(cartItemId)
        );
        Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                () -> new ProductNotFoundException(cartItem.getProductId())
        );
        if(newQuantity == cartItem.getNumber()) return cartItem;
        else if(newQuantity > product.getQuantity()){
            throw new InsufficientStockException(product.getId(), newQuantity,product.getQuantity());
        }
        cartItem.changeNumber(newQuantity);
        cartItemRepository.update(cartItem);
        return cartItem;
    }
//    calculate total price for each cart item
    @Override
    public BigDecimal calculateTotalPrice(List<CartItem> cartItemList) {
        Objects.requireNonNull(cartItemList, "cartItemList cannot be null");
        BigDecimal subTotal = BigDecimal.ZERO;
        for(CartItem cartItem : cartItemList){
            Long productId = cartItem.getProductId();
            Product product = productRepository.findById(productId).orElseThrow(
                    () -> new ProductNotFoundException(productId)
            );
            subTotal = subTotal.add(product.getBasePrice().multiply(new BigDecimal(cartItem.getNumber())));
        }
        return subTotal;
    }
//    validate cart item
    @Override
    public Product validatedCartItemToOrderItem(CartItem cartItem) {
        Objects.requireNonNull(cartItem, "cartItem cannot be null");
        // check if product is valid -> ACTIVE
        Long productId = cartItem.getProductId();
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(productId)
        );
        // check status
        if(product.getStatus() != ProductStatus.ACTIVE){
            throw new ProductInactiveException(productId);
        }
        // check quantity
        if(product.getQuantity() < cartItem.getNumber()){
            throw new InsufficientStockException(
                    cartItem.getProductId(),
                    cartItem.getNumber(),
                    product.getQuantity()
            );
        }
        return product;
    }
}
