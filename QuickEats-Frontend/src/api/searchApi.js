import axiosInstance from './axiosInstance';

export const searchMenuItemsAi = async (query, page = 0, size = 8) => {
  const response = await axiosInstance.get('/api/search', {
    params: { query, page, size }
  });
  return response.data;
};
