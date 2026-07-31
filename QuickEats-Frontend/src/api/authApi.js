import axiosInstance from './axiosInstance';

export const loginUser = async (credentials) => {
  const response = await axiosInstance.post('/api/auth/login', credentials, { timeout: 90000 });
  return response.data;
};

export const registerUser = async (userData) => {
  const response = await axiosInstance.post('/api/auth/register', userData, { timeout: 90000 });
  return response.data;
};
