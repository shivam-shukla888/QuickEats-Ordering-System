import React, { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext();

export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState(() => {
    const saved = localStorage.getItem('quickeats_cart');
    return saved ? JSON.parse(saved) : [];
  });

  const [cartRestaurant, setCartRestaurant] = useState(() => {
    const saved = localStorage.getItem('quickeats_cart_restaurant');
    return saved ? JSON.parse(saved) : null;
  });

  const [isCartOpen, setIsCartOpen] = useState(false);

  // Indian Consumer Psychology State Additions
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponDiscount, setCouponDiscount] = useState(0);
  const [deliveryTip, setDeliveryTip] = useState(0);
  const [deliveryInstructions, setDeliveryInstructions] = useState([]);
  const [paymentMethod, setPaymentMethod] = useState('UPI_GPLAY'); // Default to GPay UPI in India

  const [deliveryLocation, setDeliveryLocation] = useState(() => {
    const saved = localStorage.getItem('quickeats_location');
    return saved ? JSON.parse(saved) : {
      address: 'Connaught Place, New Delhi',
      landmark: 'Near Metro Gate #2',
      tag: 'Home'
    };
  });

  useEffect(() => {
    localStorage.setItem('quickeats_cart', JSON.stringify(cartItems));
    localStorage.setItem('quickeats_cart_restaurant', JSON.stringify(cartRestaurant));
  }, [cartItems, cartRestaurant]);

  useEffect(() => {
    localStorage.setItem('quickeats_location', JSON.stringify(deliveryLocation));
  }, [deliveryLocation]);

  // Recalculate coupon discount whenever cart items change
  useEffect(() => {
    if (appliedCoupon) {
      applyCouponCode(appliedCoupon.code);
    }
  }, [cartItems]);

  const addToCart = (menuItem, restaurant) => {
    if (cartRestaurant && cartRestaurant.id !== restaurant.id) {
      if (!window.confirm(`Your cart contains items from "${cartRestaurant.name}". Reset cart to add items from "${restaurant.name}"?`)) {
        return;
      }
      setCartItems([{ ...menuItem, quantity: 1 }]);
      setCartRestaurant(restaurant);
      return;
    }

    if (!cartRestaurant) {
      setCartRestaurant(restaurant);
    }

    setCartItems((prevItems) => {
      const existing = prevItems.find((i) => i.menuId === menuItem.menuId);
      if (existing) {
        return prevItems.map((i) =>
          i.menuId === menuItem.menuId ? { ...i, quantity: i.quantity + 1 } : i
        );
      }
      return [...prevItems, { ...menuItem, quantity: 1 }];
    });
  };

  const updateQuantity = (menuId, delta) => {
    setCartItems((prevItems) => {
      const updated = prevItems
        .map((item) => {
          if (item.menuId === menuId) {
            const newQty = item.quantity + delta;
            return newQty > 0 ? { ...item, quantity: newQty } : null;
          }
          return item;
        })
        .filter(Boolean);

      if (updated.length === 0) {
        setCartRestaurant(null);
      }
      return updated;
    });
  };

  const clearCart = () => {
    setCartItems([]);
    setCartRestaurant(null);
    setAppliedCoupon(null);
    setCouponDiscount(0);
    setDeliveryTip(0);
    setDeliveryInstructions([]);
    localStorage.removeItem('quickeats_cart');
    localStorage.removeItem('quickeats_cart_restaurant');
  };

  // Indian Coupon Logic
  const applyCouponCode = (code) => {
    const cleanCode = code.trim().toUpperCase();
    const itemTotal = getItemTotal();

    if (cleanCode === 'WELCOME50') {
      const discount = Math.min(itemTotal * 0.5, 5.0); // 50% OFF up to $5 / ₹150
      setAppliedCoupon({ code: 'WELCOME50', description: '50% OFF up to $5.00' });
      setCouponDiscount(discount);
      return { success: true, message: 'WELCOME50 applied! 50% OFF savings added.' };
    } else if (cleanCode === 'QUICKEATS') {
      const discount = Math.min(itemTotal, 3.0); // Flat $3 / ₹100 OFF
      setAppliedCoupon({ code: 'QUICKEATS', description: 'Flat $3.00 OFF' });
      setCouponDiscount(discount);
      return { success: true, message: 'QUICKEATS applied! $3.00 discount added.' };
    } else if (cleanCode === 'FREEDEL') {
      setAppliedCoupon({ code: 'FREEDEL', description: 'Free Delivery Fee' });
      setCouponDiscount(0);
      return { success: true, message: 'FREEDEL applied! Free Delivery unlocked.' };
    }

    return { success: false, message: 'Invalid coupon code. Try WELCOME50 or QUICKEATS!' };
  };

  const removeCoupon = () => {
    setAppliedCoupon(null);
    setCouponDiscount(0);
  };

  const toggleDeliveryInstruction = (instructionId) => {
    setDeliveryInstructions(prev =>
      prev.includes(instructionId)
        ? prev.filter(id => id !== instructionId)
        : [...prev, instructionId]
    );
  };

  const getItemTotal = () => {
    return cartItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
  };

  // Financial Breakdown
  const calculateBill = () => {
    const itemTotal = getItemTotal();
    const baseDeliveryFee = itemTotal > 20 || appliedCoupon?.code === 'FREEDEL' ? 0.0 : 2.99;
    const packagingCharge = itemTotal > 0 ? 0.99 : 0.0;
    const gstTax = itemTotal * 0.05; // 5% GST
    const discount = couponDiscount;
    const tip = deliveryTip;
    const grandTotal = Math.max(0, itemTotal + baseDeliveryFee + packagingCharge + gstTax + tip - discount);

    return {
      itemTotal,
      deliveryFee: baseDeliveryFee,
      packagingCharge,
      gstTax,
      discount,
      tip,
      grandTotal
    };
  };

  return (
    <CartContext.Provider
      value={{
        cartItems,
        cartRestaurant,
        isCartOpen,
        setIsCartOpen,
        addToCart,
        updateQuantity,
        clearCart,
        getItemTotal,
        calculateBill,
        appliedCoupon,
        applyCouponCode,
        removeCoupon,
        deliveryTip,
        setDeliveryTip,
        deliveryInstructions,
        toggleDeliveryInstruction,
        paymentMethod,
        setPaymentMethod,
        deliveryLocation,
        setDeliveryLocation
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
