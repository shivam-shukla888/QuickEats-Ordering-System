import axiosInstance from './axiosInstance';

export const createPaymentOrderApi = async (orderId) => {
  const response = await axiosInstance.post(`/api/payments/create-order/${orderId}`);
  return response.data;
};

export const verifyPaymentApi = async (verifyPayload) => {
  const response = await axiosInstance.post('/api/payments/verify', verifyPayload);
  return response.data;
};

export const failPaymentApi = async (orderId, reason = 'Payment failed or cancelled') => {
  const response = await axiosInstance.post(`/api/payments/fail/${orderId}`, { reason });
  return response.data;
};

export const getPaymentByOrderIdApi = async (orderId) => {
  const response = await axiosInstance.get(`/api/payments/order/${orderId}`);
  return response.data;
};
