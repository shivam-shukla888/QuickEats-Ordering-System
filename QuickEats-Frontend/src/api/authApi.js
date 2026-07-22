import axiosInstance from './axiosInstance';

export const loginUser = async (credentials) => {
  const response = await axiosInstance.post('/api/users/login', credentials);
  return response.data;
};

export const registerUser = async (userData) => {
  const response = await axiosInstance.post('/api/users/register', userData);
  return response.data;
};
