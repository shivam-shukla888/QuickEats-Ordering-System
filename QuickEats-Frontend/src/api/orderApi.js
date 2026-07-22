import axiosInstance from './axiosInstance';

export const placeOrder = async (orderData) => {
  const response = await axiosInstance.post('/api/orders', orderData);
  return response.data;
};

export const getOrderById = async (id) => {
  const response = await axiosInstance.get(`/api/orders/${id}`);
  return response.data;
};

export const getOrdersByUser = async (userId, page = 0, size = 10) => {
  const response = await axiosInstance.get(`/api/orders/user/${userId}`, {
    params: { page, size }
  });
  return response.data;
};

export const getMyOrders = async (page = 0, size = 10) => {
  const response = await axiosInstance.get('/api/orders/my-orders', {
    params: { page, size }
  });
  return response.data;
};

export const getAllOrders = async (page = 0, size = 10) => {
  const response = await axiosInstance.get('/api/orders', {
    params: { page, size }
  });
  return response.data;
};

export const updateOrderStatus = async (id, status) => {
  const response = await axiosInstance.put(`/api/orders/${id}/status`, { status });
  return response.data;
};

export const cancelOrder = async (id) => {
  const response = await axiosInstance.put(`/api/orders/${id}/cancel`);
  return response.data;
};
