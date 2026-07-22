import axiosInstance from './axiosInstance';

export const getMenuByRestaurant = async (restaurantId) => {
  const response = await axiosInstance.get(`/api/restaurants/${restaurantId}/menu`);
  return response.data;
};

export const addMenuItem = async (restaurantId, menuData) => {
  const response = await axiosInstance.post(`/api/restaurants/${restaurantId}/menu`, menuData);
  return response.data;
};

export const updateMenuItem = async (menuId, menuData) => {
  const response = await axiosInstance.put(`/api/restaurants/menu/${menuId}`, menuData);
  return response.data;
};

export const deleteMenuItem = async (menuId) => {
  const response = await axiosInstance.delete(`/api/restaurants/menu/${menuId}`);
  return response.data;
};
