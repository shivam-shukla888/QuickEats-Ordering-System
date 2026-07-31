import axiosInstance from './axiosInstance';

export const getCartApi = async () => {
  const response = await axiosInstance.get('/api/cart');
  return response.data;
};

export const addCartItemApi = async (menuItemId, quantity = 1, restaurantId = null, specialInstructions = '') => {
  const response = await axiosInstance.post('/api/cart/items', {
    menuItemId,
    quantity,
    restaurantId,
    specialInstructions,
  });
  return response.data;
};

export const updateCartItemApi = async (itemId, quantity) => {
  const response = await axiosInstance.put(`/api/cart/items/${itemId}`, { quantity });
  return response.data;
};

export const removeCartItemApi = async (itemId) => {
  const response = await axiosInstance.delete(`/api/cart/items/${itemId}`);
  return response.data;
};

export const clearCartApi = async () => {
  const response = await axiosInstance.delete('/api/cart/clear');
  return response.data;
};

export const checkoutCartApi = async (deliveryAddress, paymentMethod = 'CREDIT_CARD') => {
  const response = await axiosInstance.post('/api/cart/checkout', {
    deliveryAddress,
    paymentMethod,
  });
  return response.data;
};
