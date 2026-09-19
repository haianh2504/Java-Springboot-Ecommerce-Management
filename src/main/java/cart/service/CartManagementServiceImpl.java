package cart.service;

import cart.entities.Cart;
import cart.repository.CartRepository;
import cart_item.repository.CartItemRepository;
import exception.business.detailed_exceptions.AccountBannedException;
import exception.business.detailed_exceptions.CartAlreadyCheckedOutException;
import exception.resource.detailed_exceptions.CartNotFoundException;
import exception.resource.detailed_exceptions.UserNotFoundException;
import user.entities.User;
import user.entities.UserStatus;
import user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

public class CartManagementServiceImpl implements CartManagementService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
//    constructor
    public CartManagementServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = Objects.requireNonNull(cartRepository, "CartRepository cannot be null");
        this.cartItemRepository = Objects.requireNonNull(cartItemRepository, "CartItemRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }
//    create new cart
    @Override
    public Cart createCart(Long userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        User persistedUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (persistedUser.getStatus() == UserStatus.BANNED) {
            throw new AccountBannedException();
        }
        return cartRepository.save(new Cart(userId));
    }
//    get carts by userID
    @Override
    public List<Cart> getCartByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        return List.copyOf(cartRepository.findByUserId(userId));
    }
//    get cart by cartId
    @Override
    public Cart getCartById(Long cartId) {
        Objects.requireNonNull(cartId, "cartId cannot be null");
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId)
                );
    }

    //    clear cart
    @Override
    public void clearCart(Long cartId) {
        Objects.requireNonNull(cartId, "cartId cannot be null");
        cartItemRepository.deleteAllByCartId(cartId);
    }
//    check out cart - change cart status
    @Override
    public void checkoutCart(Long cartId) {
        Objects.requireNonNull(cartId, "cartId cannot be null");
        if (cartRepository.markCheckedOutIfActive(cartId)) {
            return;
        }

        // A zero-row UPDATE has two possible meanings. This lookup translates the
        // persistence result into the correct domain error for the application edge.
        if (cartRepository.findById(cartId).isEmpty()) {
            throw new CartNotFoundException(cartId);
        }
        throw new CartAlreadyCheckedOutException();
    }
}
