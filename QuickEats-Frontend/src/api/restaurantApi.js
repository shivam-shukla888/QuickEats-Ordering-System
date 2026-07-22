import axiosInstance from './axiosInstance';

export const getRestaurants = async (page = 0, size = 10, sort = 'id,asc') => {
  const response = await axiosInstance.get('/api/restaurants', {
    params: { page, size, sort }
  });
  return response.data;
};

export const getRestaurantById = async (id) => {
  const response = await axiosInstance.get(`/api/restaurants/${id}`);
  return response.data;
};

export const getCuisines = async () => {
  const response = await axiosInstance.get('/api/restaurants/cuisines');
  return response.data;
};

export const getRestaurantsByCuisine = async (cuisineType, page = 0, size = 10) => {
  const response = await axiosInstance.get(`/api/restaurants/cuisine/${cuisineType}`, {
    params: { page, size }
  });
  return response.data;
};

export const searchRestaurants = async (name, page = 0, size = 10) => {
  const response = await axiosInstance.get('/api/restaurants/search', {
    params: { name, page, size }
  });
  return response.data;
};

export const createRestaurant = async (restaurantData) => {
  const response = await axiosInstance.post('/api/restaurants', restaurantData);
  return response.data;
};

export const updateRestaurant = async (id, restaurantData) => {
  const response = await axiosInstance.put(`/api/restaurants/${id}`, restaurantData);
  return response.data;
};

export const deleteRestaurant = async (id) => {
  const response = await axiosInstance.delete(`/api/restaurants/${id}`);
  return response.data;
};
