import axiosInstance from './axiosInstance';

export const sendAiChatMessage = async (message) => {
  const response = await axiosInstance.post('/api/ai/chat', { message });
  return response.data;
};
