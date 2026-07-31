package com.quickeats.service;

import com.quickeats.dto.*;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Cart;
import com.quickeats.model.CartItem;
import com.quickeats.model.Menu;
import com.quickeats.model.User;
import com.quickeats.repository.CartItemRepository;
import com.quickeats.repository.CartRepository;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderService orderService;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional(readOnly = true)
    public CartDTO getCartDTO(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return CartDTO.fromEntity(cart);
    }

    @Transactional
    public CartDTO addItemToCart(Long userId, AddCartItemDTO dto) {
        Cart cart = getOrCreateCart(userId);
        Menu menu = menuRepository.findById(dto.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + dto.getMenuItemId()));

        Long restaurantId = dto.getRestaurantId() != null ? dto.getRestaurantId() : (menu.getRestaurant() != null ? menu.getRestaurant().getId() : null);

        // If cart has items from another restaurant, clear old items to maintain single-restaurant carts
        if (cart.getRestaurantId() != null && restaurantId != null && !cart.getRestaurantId().equals(restaurantId)) {
            cart.getItems().clear();
        }
        cart.setRestaurantId(restaurantId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(dto.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            if (dto.getSpecialInstructions() != null && !dto.getSpecialInstructions().trim().isEmpty()) {
                item.setSpecialInstructions(dto.getSpecialInstructions());
            }
        } else {
            CartItem newItem = new CartItem(
                    cart,
                    menu.getId(),
                    menu.getItemName(),
                    menu.getPrice(),
                    dto.getQuantity(),
                    dto.getSpecialInstructions()
            );
            cart.getItems().add(newItem);
        }

        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);
        return CartDTO.fromEntity(savedCart);
    }

    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);
        return CartDTO.fromEntity(savedCart);
    }

    @Transactional
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        cart.recalculateTotal();

        Cart savedCart = cartRepository.save(cart);
        return CartDTO.fromEntity(savedCart);
    }

    @Transactional
    public CartDTO clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.recalculateTotal();

        Cart savedCart = cartRepository.save(cart);
        return CartDTO.fromEntity(savedCart);
    }

    @Transactional
    public OrderResponseDTO checkoutCart(Long userId, String deliveryAddress, String paymentMethod) {
        Cart cart = getOrCreateCart(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Add items before checkout.");
        }

        List<OrderItemDTO> orderItems = cart.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getMenuItemId(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getName()
                ))
                .collect(Collectors.toList());

        CreateOrderDTO createOrderDTO = new CreateOrderDTO(userId, cart.getRestaurantId(), orderItems);
        if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
            createOrderDTO.setDeliveryAddress(deliveryAddress);
        }
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            createOrderDTO.setPaymentMethod(paymentMethod);
        }

        OrderResponseDTO responseDTO = orderService.placeOrder(createOrderDTO);

        // Clear cart after placing order successfully
        cart.getItems().clear();
        cart.recalculateTotal();
        cartRepository.save(cart);

        return responseDTO;
    }
}
