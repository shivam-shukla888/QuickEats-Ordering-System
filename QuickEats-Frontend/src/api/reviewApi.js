import axiosInstance from './axiosInstance';

export const createReviewApi = async (reviewPayload) => {
  const response = await axiosInstance.post('/api/reviews', reviewPayload);
  return response.data;
};

export const getRestaurantReviewsApi = async (restaurantId) => {
  const response = await axiosInstance.get(`/api/restaurants/${restaurantId}/reviews`);
  return response.data;
};

export const getRestaurantRatingApi = async (restaurantId) => {
  const response = await axiosInstance.get(`/api/restaurants/${restaurantId}/rating`);
  return response.data;
};
