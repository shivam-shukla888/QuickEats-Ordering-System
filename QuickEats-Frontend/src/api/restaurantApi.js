import axiosInstance from './axiosInstance';

export const getRestaurants = async (page = null, size = null, sort = 'id,asc') => {
  const params = {};
  if (page !== null && page !== undefined) params.page = page;
  if (size !== null && size !== undefined) params.size = size;
  if (sort) params.sort = sort;

  const response = await axiosInstance.get('/api/restaurants', {
    params,
    timeout: 90000
  });
  return response.data;
};

export const getRestaurantById = async (id) => {
  const response = await axiosInstance.get(`/api/restaurants/${id}`, { timeout: 90000 });
  return response.data;
};

export const getCuisines = async () => {
  const response = await axiosInstance.get('/api/restaurants/cuisines', { timeout: 90000 });
  return response.data;
};

export const getRestaurantsByCuisine = async (cuisineType, page = 0, size = 10) => {
  const response = await axiosInstance.get(`/api/restaurants/cuisine/${cuisineType}`, {
    params: { page, size },
    timeout: 90000
  });
  return response.data;
};

export const searchRestaurants = async (name, page = 0, size = 10) => {
  const response = await axiosInstance.get('/api/restaurants/search', {
    params: { name, page, size },
    timeout: 90000
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
